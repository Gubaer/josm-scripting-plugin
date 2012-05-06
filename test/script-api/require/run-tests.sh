#!/bin/sh
#
# Run the test cases for the CommonJS module loader
#
echo "------------------------------------------------------"
echo "resolve-test.js"
echo "------------------------------------------------------"
../../../rhino.sh resolve-test.js

echo "------------------------------------------------------"
echo "require-test.js"
echo "------------------------------------------------------"
../../../rhino.sh require-test.js
