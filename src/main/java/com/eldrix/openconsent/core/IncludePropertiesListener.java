package com.eldrix.openconsent.core;

import java.util.Arrays;
import java.util.List;

import org.apache.cayenne.exp.Property;
import org.apache.cayenne.map.Entity;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.annotation.listener.SelectRequestParsed;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.runtime.processor.select.SelectContext;

/**
 * Simple LinkRest listener that injects additional properties and relationships.
 *
 * @param <T>
 */
public class IncludePropertiesListener<T> {
	private List<Property<?>> _properties;

	public IncludePropertiesListener(Property<?>... properties) {
		_properties = Arrays.asList(properties);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@SelectRequestParsed
	public void includeRelationships(SelectContext<T> context) {
		ResourceEntity<T> rEntity = context.getEntity();
		for (Property<?> prop : _properties) {
			processProperty(rEntity, prop.getName());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void processProperty(ResourceEntity<?> entity, String path) {
		String property = null;
		String remainder = null;
		int dot = path.indexOf(Entity.PATH_SEPARATOR);
		if (dot==0 || dot == path.length() - 1) {
			throw new IllegalArgumentException("Invalid key path");
		} else if (dot > 0) {
			property = path.substring(0, dot);
			remainder = path.substring(dot+1, path.length());
		}  else {
			property = path;		// not a key path, only a key
		}
		LrEntity<?> lrEntity = entity.getLrEntity();
		LrAttribute attr = lrEntity.getAttribute(property);
		if (attr != null) {
			if (entity.getAttributes().containsKey(property) == false) {
				entity.getAttributes().put(property, attr);
			}
			if (remainder != null) {
				throw new IllegalStateException("Cannot have sub-attribute of an attribute, only a relationship.");
			}
		} else {
			LrRelationship rel = lrEntity.getRelationship(property);
			if (rel != null) {
				ResourceEntity<?> childEntity = entity.getChild(property);
				if (childEntity == null) {
					LrEntity<?> targetType = rel.getTargetEntity();
					childEntity = new ResourceEntity(targetType, rel);
					entity.getChildren().put(property, childEntity);
					childEntity.includeId();
					for (LrAttribute a : childEntity.getLrEntity().getAttributes()) {
						childEntity.getAttributes().put(a.getName(), a);
						childEntity.getDefaultProperties().add(a.getName());
					}
				}
				if (remainder != null) {
					processProperty(childEntity, remainder);
				}
			}
		}
	}

}
