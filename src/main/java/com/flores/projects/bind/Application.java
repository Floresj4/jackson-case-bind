package com.flores.projects.bind;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * 
 * @author Jason
 *
 */
public class Application {

	private static final Logger logger = LoggerFactory.getLogger(Application.class);
	
	public static void main(String[] args) throws IOException {
		PropertyConfigurator.configure("src/main/resources/log4j.properties");

		ObjectMapper mapper = new ObjectMapper();

		SimpleModule module = new SimpleModule();
        module.setDeserializerModifier(new BeanDeserializerModifier() {
            
			@Override @SuppressWarnings("rawtypes")
            public JsonDeserializer<Enum> modifyEnumDeserializer(DeserializationConfig config,
                                                              final JavaType type,
                                                              BeanDescription beanDesc,
                                                              final JsonDeserializer<?> deserializer) {
                return new JsonDeserializer<Enum>() {
					@Override @SuppressWarnings("unchecked")
                    public Enum deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
                        
						Class<? extends Enum> rawClass = (Class<Enum<?>>) type.getRawClass();
                        return Enum.valueOf(rawClass, jp.getValueAsString().toUpperCase());
                    }
                };
            }
        });
        
        module.addSerializer(Enum.class, new StdSerializer<Enum>(Enum.class) {
			private static final long serialVersionUID = -2362448401225572969L;

			@Override @SuppressWarnings("rawtypes")
			public void serialize(Enum value, JsonGenerator gen,
					SerializerProvider provider) throws IOException {
				gen.writeString(value.name().toLowerCase());
			}
		});
        
        mapper.registerModule(module);

        List<Sport>sports = Arrays.asList(Sport.GYMNASTICS, Sport.SOCCER, Sport.BASKETBALL);
        String json = mapper.writeValueAsString(sports);
        logger.info(json);

        //deserialize and collect
        File source = new File("src/main/resources/athlete.json");

        List<Athlete>athletes = new ArrayList<>();
        JsonNode root = mapper.readTree(source);
        root.forEach(n -> logger.info(n.toString()));

        Athlete a = null;
        assertEquals(a.name, "Ralph Nader");
        assertEquals(a.age, 7);
        assertEquals(a.sport, Sport.GYMNASTICS);
        athletes.add(a);

        logger.info("successfully deserialized athlete file");

        //serialize with collection
        Athlete b = new Athlete("James Vanderbeek", 12, Sport.SOCCER);
        athletes.add(b);

        try { mapper.writeValue(new File("src/main/resources/athlete.json"), athletes); }
        catch(JsonMappingException | JsonGenerationException je) {
        	fail("error during serialization");
        }

        //read back and test
//        JsonNode root = mapper.readTree(source);
        
	}
}
