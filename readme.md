### Jackson-case-bind

A custom mapping module for jackson.databind.ObjectMapper to handle serializing/deserializing of Enumerated values.

Enumerated values are

* Serialized as lowercase values
* Deserialized toUpperCase, excluding SoCcEr which contains a special case