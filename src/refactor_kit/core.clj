(ns refactor-kit.core
  (:require
   [refactor-kit.validator :refer [register! defnvalidated]]))

(def dat {:field "string value"
          :tags ["tag" "another tag" "tag"]
          :info {:name "John doe"
                 :address [2 3 4]
                 :phone-numbers [0444 444]
                 :extra-info {:nickname "doe"}}})
(comment
  "1. Register schema
   2. Make validated function
   3. Run the function and it will check if data matches the schema"

  (register! :address [:string {:min 1}])

  (defnvalidated return-args [args]
    args)

  (return-args dat))
