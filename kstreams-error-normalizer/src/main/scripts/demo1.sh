#!/usr/bin/env bash
#
# Copyright © 2021 Travis Hoffman (travis@firkin.io)
# Copyright © 2021 Firkin IO (https://firkin.io/)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

DEMO_MAIN=io.firkin.kstreams.normalizer.demo.v1.CentralErrorNormalizerDemo
CLASSPATH=./kstreams-error-normalizer/target/classes
CLASSPATH+=:~/.m2/repository/org/testcontainers/testcontainers/1.15.3/testcontainers-1.15.3.jar
CLASSPATH+=:~/.m2/repository/org/junit/jupiter/junit-jupiter-api/5.7.1/junit-jupiter-api-5.7.1.jar
echo "java -cp $CLASSPATH $DEMO_MAIN"
java -cp $CLASSPATH $DEMO_MAIN
