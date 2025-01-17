package com.sap.oss.phosphor.fosstars.model.value;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import com.sap.oss.phosphor.fosstars.model.Feature;
import com.sap.oss.phosphor.fosstars.model.Value;
import com.sap.oss.phosphor.fosstars.model.feature.AbstractFeature;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class AbstractValueTest {

  private static class FeatureImpl extends AbstractFeature {

    FeatureImpl(String name) {
      super(name);
    }

    @Override
    public Value value(Object object) {
      return new ValueImpl(this, object);
    }

    @Override
    public Value parse(String string) {
      throw new UnsupportedOperationException();
    }
  }

  private static class ValueImpl extends AbstractValue {

    final Object value;

    ValueImpl(Feature feature, Object value) {
      super(feature);
      this.value = value;
    }

    @Override
    public Object get() {
      return value;
    }
  }

  @Test
  public void testProcessIfKnown() {
    ValueImpl value = new ValueImpl(new FeatureImpl("feature"), "test");
    assertFalse(value.isUnknown());

    List processedValues = new ArrayList();

    value.processIfKnown(object -> {
      assertEquals("test", object);
      processedValues.add(object);
    }).processIfUnknown(() -> fail("This should not be called!"));

    Value unknown = new FeatureImpl("feature").unknown();
    unknown.processIfKnown(object -> {
      fail("this should not be reached");
    }).processIfUnknown(() -> {
      processedValues.add("unknown");
    });

    assertEquals(2, processedValues.size());
    assertEquals("test", processedValues.get(0));
    assertEquals("unknown", processedValues.get(1));
  }

  @Test
  public void testOrElse() {
    FeatureImpl feature = new FeatureImpl("feature");
    assertEquals("value", new ValueImpl(feature, "value").orElse("another"));
    assertEquals("another", feature.unknown().orElse("another"));
  }
}