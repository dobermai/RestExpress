/*
 * Copyright 2010-2013, Strategic Gains, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.strategicgains.restexpress.serialization.json;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.strategicgains.restexpress.ContentType;
import com.strategicgains.restexpress.serialization.DeserializationException;
import com.strategicgains.restexpress.serialization.SerializationException;
import com.strategicgains.restexpress.serialization.SerializationProcessor;
import com.strategicgains.util.date.DateAdapterConstants;

/**
 * A SerializationProcessor to handle JSON input/output. It anticipates ISO
 * 8601-compatible time points for date instances and outputs dates as ISO 8601
 * time points.
 * 
 * @author toddf
 * @since Mar 16, 2010
 */
public class DefaultJsonProcessor
implements SerializationProcessor
{
	private ObjectMapper mapper;

	public DefaultJsonProcessor()
	{
		super();
		SimpleModule module = new SimpleModule();
		initializeModule(module);
	}

	public DefaultJsonProcessor(SimpleModule module)
	{
		initialize(module);
	}

	public DefaultJsonProcessor(ObjectMapper mapper)
	{
		super();
		this.mapper = mapper;
	}

	private void initialize(SimpleModule module)
	{
		this.mapper = new ObjectMapper();
		mapper.registerModule(module);
		initializeMapper(mapper);
	}

	/**
	 * Template method for sub-classes to augment the module with desired
	 * serializers and/or deserializers.  Sub-classes should call super()
	 * to get default settings.
	 * 
	 * @param module a SimpleModule
	 */
	protected void initializeModule(SimpleModule module)
    {
		module
			.addSerializer(Date.class, new JacksonTimepointSerializer())
			.addDeserializer(Date.class, new JacksonTimepointDeserializer());
		initialize(module);
    }

	/**
	 * Template method for sub-classes to augment the mapper with desired
	 * settings.  Sub-classes should call super() to get default settings.
	 * 
	 * @param module a SimpleModule
	 */
	protected void initializeMapper(ObjectMapper mapper)
    {
		mapper
			.enable(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS)
			.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
			.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.setSerializationInclusion(JsonInclude.Include.NON_NULL)
			.setDateFormat(new SimpleDateFormat(DateAdapterConstants.TIME_POINT_OUTPUT_FORMAT));
    }

	@Override
	public <T> T deserialize(String string, Class<T> type)
	{
		try
		{
			return (string == null || string.trim().isEmpty() ? null : mapper.readValue(string, type));
		}
		catch (JsonProcessingException e)
		{
			throw new DeserializationException(e);
		}
		catch (IOException e)
		{
			throw new DeserializationException(e);
		}
	}

	@Override
	public <T> T deserialize(ChannelBuffer buffer, Class<T> type)
	{
		try
		{
			
			return (buffer == null || buffer.readableBytes() == 0 ? null : mapper.readValue(new InputStreamReader(new ChannelBufferInputStream(buffer), ContentType.CHARSET), type));
		}
		catch (JsonProcessingException e)
		{
			throw new DeserializationException(e);
		}
		catch (IOException e)
		{
			throw new DeserializationException(e);
		}
	}

	@Override
	public String serialize(Object object)
	{
		try
		{
			return (object == null ? "" : mapper.writeValueAsString(object));
		}
		catch (JsonProcessingException e)
		{
			throw new SerializationException(e);
		}
	}

	@Override
	public String getResultingContentType()
	{
		return ContentType.JSON;
	}
}
