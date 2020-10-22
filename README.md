# kafkax-jmeter
Simple and extendable JMeter samplers for Kafka produce and consume\
Inspiration and fragments of base class from repo:\
https://github.com/BrightTag/kafkameter

## Module kafkax-jmeter-core

### KafkaxSampler
Abstract implementation of AbstractJavaSamplerClient from JMeter library.
* Knows: about Kafka, topics, details of SSL protocol
* Doesn't know: produce or consume?
* Introduces virtual populateParams method, which can be extended by specializations. All params defined for sampler class are visible in JMeter GUI.

### KafkaxProducer
Still abstract specialization of KafkaxSampler.
* Knows: that his job is to produce to topik; that byte array serialization is used
* Introduces template implementation of runTest method
* Delegates runTestImpl to specializations
* Doesn't know: what to serialize

### KafkaxStringProducer
Still abstract specialization of KafkaxProducer.
* Knows: it should serialize String data
* Doesn't know: which String?

### KafkaxSimpleStringProducer
Specialization of KafkaxStringSampler.
* Knows: it should serialize String provided in parameter named kafka.message
* Adds this parameter in overrided populateParams method

### KafkaxSpecificAvroProducer
Abstract specialization of KafkaxProducer.
* Knows: it should serialize AVRO object, extending SpecificRecord, generated from AVRO scheme (AVSC)
* Doesn't know: how to obtain this object

### KafkaxConsumer
Still abstract specialization of KafkaxSampler.
* Knows: that his job is to consume from topik; that byte array deserialization is used
* Introduces template implementation of runTest method
* Delegates runTestImpl to specializations
* Doesn't know: what to deserialize

### KafkaxSimpleStringConsumer
Specialization of KafkaxConsumer
* Knows: it should deserialize String data and save it as JMeter samplers Response Data

### KafkaxSpecificAvroConsumer
Abstract specialization of KafkaxConsumer.
* Knows: it should deserialize AVRO object, extending SpecificRecord, generated from AVRO scheme (AVSC)
* Doesn't know: class of this object

## Module kafkax-jmeter-examples

## NotSoSimpleStringProducer
Example specialization of KafkaxStringSampler.
* Knows: it should serialize String combined from hardcoded template and additionally parameters, added in overriden populateParams method

## WeatherKafkaxProducer
Example specialization of KafkaxSpecificAvroProducer
Knows: serializes objects of class Weather, generated from schema weather.avsc. Attributes of this objects are added to overriden populateParams method and provided in JMeter GUI

## WeatherKafkaxConsumer
Example specialization of KafkaxSpecificAvroConsumer
Knows: deserializes objects of class Weather, generated from schema weather.avsc
