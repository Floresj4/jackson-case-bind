package com.flores.projects.bind;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
import com.fasterxml.jackson.core.JsonProcessingException;
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
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * 
 * @author Jason
 *
 */
public class Application {

	static ObjectMapper mapper;
	
	static File source = new File("src/main/resources/athlete.json");
	
	private static final Logger logger = LoggerFactory.getLogger(Application.class);
	
	static {
		PropertyConfigurator.configure("src/main/resources/log4j.properties");
		
		if(source.delete()) 
			logger.debug("removed data file.");
	}

	@SuppressWarnings("rawtypes")
	private static void initialize() {
		mapper = new ObjectMapper();

		SimpleModule module = new SimpleModule();
        module.setDeserializerModifier(new BeanDeserializerModifier() {
            
			@Override public JsonDeserializer<Enum> modifyEnumDeserializer(DeserializationConfig config,
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

			@Override public void serialize(Enum value, JsonGenerator gen,
					SerializerProvider provider) throws IOException {
				gen.writeString(value.name().toLowerCase());
			}
		});
        
        mapper.registerModule(module);
	}
	
	public static final List<Athlete> loadCollection(JsonNode root) {
		List<Athlete> entries = new ArrayList<>();
		
		logger.debug("tree loaded...");
        root.forEach(n -> { 
        	logger.info(n.toString());

        	entries.add(new Athlete(
            		n.path("name").asText(),
            		n.path("age").asInt(), 
            		mapper.convertValue(n.path("sport")
            				, Sport.class)));
        });
		
		return entries;
	}

	private static final List<Athlete> createCollection() {
    	return Arrays.asList(
    			new Athlete("Simone Biles", 7, Sport.GYMNASTICS),
    			new Athlete("Melissa Regan", 25, Sport.BASKETBALL),
    			new Athlete("Hope Solo", 30, Sport.SoCcEr),
    			new Athlete("Michael Jordan", 53, Sport.BASKETBALL));
	}
	
	private static void testWrite() throws JsonProcessingException {
        //test write...
        List<Sport>sports = Arrays.asList(Sport.GYMNASTICS, Sport.SoCcEr, Sport.BASKETBALL);
        String json = mapper.writeValueAsString(sports);
		logger.info("current config output: \r\n\t{}", json);
	}
	
	public static void main(String[] args) throws IOException {

		initialize();

		testWrite();
		
        try { mapper.writeValue(source, createCollection()); }
        catch(JsonMappingException | JsonGenerationException je) {
        	fail("error during serialization");
        }

        //deserialize and collect
        List<Athlete>athletes = loadCollection(mapper.readTree(source));
        assertTrue(athletes.size() > 0);

    	Athlete a = athletes.get(0);
        assertEquals(a.name, "Simone Biles");
        assertEquals(a.age, 19);
        assertEquals(a.sport, Sport.GYMNASTICS);
        
        Athlete b = athletes.get(1);
        assertEquals(b.name, "Melissa Regan");
        assertEquals(b.age, 25);
        assertEquals(b.sport, Sport.BASKETBALL);        
        logger.info("successfully deserialized athlete file");


	}
}
