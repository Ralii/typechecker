# Typechecker
Automatic typechecker for function input data

## Current features
Automatic function input parameter validation with malli global registry. Insert a key for example `:user-id string?` and it will check every level of input parameters for they key and if it matches the schema. Can be used to speed up refactoring and verify that data flow is correct.

```
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
```
