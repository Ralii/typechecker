(ns refactor-kit.validator
  (:require
   [malli.core :as malli]
   [malli.registry :as mr]))

(defn- illegal-argument
  "Creates an IllegalArgumentException or js/Error."
  [message]
  #?(:clj (IllegalArgumentException. message)
     :cljs (js/Error message)))

(defn- process-first-on-pred
  "Processes the first in `coll` with `f` if `pred`, otherwise `default`.

   Returns a pair [processed rest] if pred succeeds, or [default coll]."
  [coll f pred & {:keys [default] :or {default nil}}]
  (if (pred (first coll))
    [(f (first coll)) (rest coll)]
    [default coll]))

(defn- parse-fn
  "Return: {:name string? :fdefs {:params ... :prepost ... :body ...}}"
  [fdecl]
  (let [[name fdecl] (process-first-on-pred fdecl identity symbol?)
        fdecl (cond (vector? (first fdecl)) (list fdecl)
                    (seq? fdecl) fdecl
                    :else (throw (illegal-argument "Parameters missing")))
        parse-body (fn [[params & body]]
                     (when-not (vector? params)
                       (throw (illegal-argument
                               (if (seq? (first fdecl))
                                 (str "Parameter declaration " params
                                      " should be a vector.")
                                 (str "Invalid signature: " fdecl
                                      " should be a list.")))))
                     (let [[prepost body]
                           (process-first-on-pred body identity map?)]
                       {:params params
                        :prepost prepost
                        :body body}))
        fdefs (map parse-body fdecl)]
    {:name name :fdefs fdefs}))

(defn flatten-&-preserve-levels [args]
  (->> args
       (tree-seq seqable? seq)
       (filter map-entry?)))

(def registry*
  (atom {:string (malli/-string-schema)
         :maybe (malli/-maybe-schema)
         :map (malli/-map-schema)}))

(mr/set-default-registry!
 (mr/mutable-registry registry*))

(defn register! [type ?schema]
  (swap! registry* assoc type ?schema))

(defn- found-from-schema? [key]
  (some? (key @registry*)))

(defn check-schema-data [schema-data]
  (->> schema-data
       (map (fn [[schema-key schema-data]]
              (when (found-from-schema? schema-key)
                (when-not (= true (malli/validate schema-key
                                                  schema-data))
                  {:error true
                   :schema-key schema-key
                   :schema-data schema-data}))))
       (filter not-empty)))

(defn- build-function-body
  [{:keys [params prepost body name]}]
  (if (some? prepost)
    `(~params ~prepost
              (do
                (let [flatted# (flatten-&-preserve-levels ~params)
                      error# (first (check-schema-data flatted#))]
                  (if (:error error#)
                    (throw (Exception. (str "ERROR IN KEY: " (:schema-key error#)
                                            " | IN FUNCTION NAME: " ~name)))
                    ~@body))))
    `(~params (do
                (let [flatted# (flatten-&-preserve-levels ~params)
                      error# (first (check-schema-data flatted#))]
                  (if (:error error#)
                    (throw (Exception. (str "ERROR IN KEY: " (:schema-key error#)
                                            " | IN FUNCTION NAME: " ~name)))
                    ~@body))))))

(defn- parsed-defn->defn
  "Builds a defn from a parsed defn."
  [{:keys [name meta fdefs]}]
  (let [fdefs (map (fn [body] (assoc body :name name)) fdefs)
        bodies (map build-function-body fdefs)

        bodies (if (= 1 (count bodies))
                 (first bodies)
                 bodies)]
    (if (empty? meta)
      `(defn ~name ~@bodies)
      `(defn ~name ~meta ~@bodies))))

(defn- parse-defn
  [[f & fdecl]]
  (let
   [[m fdecl] (process-first-on-pred
               fdecl #(assoc {} :doc %) string? :default {})
    [m fdecl] (process-first-on-pred
               fdecl #(conj % m) map? :default m)
    {:keys [fdefs]}  (parse-fn fdecl)]
    {:name f
     :meta m
     :fdefs fdefs}))

(defmacro defnvalidated
  "Validator function that wraps normal defn
   with checker that runs the input params against
   global registry

   TODO: Make support for docstring since it is
   stripped out here at the beginning"
  [name & other]
  (let [[_ fn] (if (string? (first other))
                 [(first other) (rest other)]
                 [nil other])
        fn-parsed (parse-defn `(~name ~@fn))]
    (parsed-defn->defn fn-parsed)))
