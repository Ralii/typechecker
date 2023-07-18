# refactor-kit
Refactor kit and test bench for refactoring tools with Clojure

## Current features
Automatic function input parameter validation with malli global registry. Insert a key for example `:user-id string?` and it will check every level of input parameters for they key and if it matches the schema. Can be used to speed up refactoring and verify that data flow is correct.
