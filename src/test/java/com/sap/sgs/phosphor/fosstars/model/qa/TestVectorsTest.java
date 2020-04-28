package com.sap.sgs.phosphor.fosstars.model.qa;

import static com.sap.sgs.phosphor.fosstars.model.other.Utils.setOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.sgs.phosphor.fosstars.model.feature.example.ExampleFeatures;
import com.sap.sgs.phosphor.fosstars.model.math.DoubleInterval;
import com.sap.sgs.phosphor.fosstars.model.rating.example.SecurityRatingExample.SecurityLabelExample;
import com.sap.sgs.phosphor.fosstars.model.value.IntegerValue;
import java.io.IOException;
import org.junit.Test;

public class TestVectorsTest {

  private static final TestVectors VECTORS = new TestVectors();

  static {
    VECTORS.add(
        new StandardTestVector(
            setOf(
                new IntegerValue(ExampleFeatures.NUMBER_OF_COMMITS_LAST_MONTH_EXAMPLE, 10),
                new IntegerValue(ExampleFeatures.NUMBER_OF_CONTRIBUTORS_LAST_MONTH_EXAMPLE, 2)),
            DoubleInterval.init().from(4.0).to(6.4).closed().make(),
            SecurityLabelExample.OKAY,
            "test_1"));
    VECTORS.add(
        new StandardTestVector(
            setOf(
                new IntegerValue(ExampleFeatures.NUMBER_OF_COMMITS_LAST_MONTH_EXAMPLE, 30),
                new IntegerValue(ExampleFeatures.NUMBER_OF_CONTRIBUTORS_LAST_MONTH_EXAMPLE, 3)),
            DoubleInterval.init().from(5.0).to(7.4).closed().make(),
            SecurityLabelExample.OKAY,
            "test_2"));
  }

  @Test
  public void testGetAndAdd() {
    TestVectors vectors = new TestVectors();

    TestVector firstVector = new StandardTestVector(
        setOf(
            new IntegerValue(ExampleFeatures.NUMBER_OF_COMMITS_LAST_MONTH_EXAMPLE, 10),
            new IntegerValue(ExampleFeatures.NUMBER_OF_CONTRIBUTORS_LAST_MONTH_EXAMPLE, 2)),
        DoubleInterval.init().from(4.0).to(6.4).closed().make(),
        SecurityLabelExample.OKAY,
        "test_1");
    vectors.add(firstVector);

    TestVector secondVector = new StandardTestVector(
        setOf(
            new IntegerValue(ExampleFeatures.NUMBER_OF_COMMITS_LAST_MONTH_EXAMPLE, 30),
            new IntegerValue(ExampleFeatures.NUMBER_OF_CONTRIBUTORS_LAST_MONTH_EXAMPLE, 3)),
        DoubleInterval.init().from(5.0).to(7.4).closed().make(),
        SecurityLabelExample.OKAY,
        "test_2");
    vectors.add(secondVector);

    assertFalse(vectors.isEmpty());
    assertEquals(2, vectors.size());
    for (TestVector vector : vectors) {
      assertTrue(vector instanceof TestVectorWithDefaults);
    }

    assertEquals(firstVector, ((TestVectorWithDefaults) vectors.get(0)).originalVector());
    assertEquals(secondVector, ((TestVectorWithDefaults) vectors.get(1)).originalVector());
  }

  @Test
  public void testSerializationJson() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    TestVectors clone = mapper.readValue(
        mapper.writeValueAsBytes(VECTORS), TestVectors.class);
    assertTrue(VECTORS.equals(clone) && clone.equals(VECTORS));
    assertEquals(VECTORS.hashCode(), clone.hashCode());
  }

  @Test
  public void testSerializationYaml() throws IOException {
    byte[] bytes = TestVectors.YAML_OBJECT_MAPPER.writeValueAsBytes(VECTORS);
    System.out.println(new String(bytes));
    TestVectors clone = TestVectors.YAML_OBJECT_MAPPER.readValue(bytes, TestVectors.class);
    assertTrue(VECTORS.equals(clone) && clone.equals(VECTORS));
    assertEquals(VECTORS.hashCode(), clone.hashCode());
  }

  @Test
  public void testAdd() {
    TestVectors combinedVectors = new TestVectors();
    combinedVectors.add(
        new StandardTestVector(
            setOf(
                new IntegerValue(ExampleFeatures.NUMBER_OF_COMMITS_LAST_MONTH_EXAMPLE, 10),
                new IntegerValue(ExampleFeatures.NUMBER_OF_CONTRIBUTORS_LAST_MONTH_EXAMPLE, 2)),
            DoubleInterval.init().from(4.0).to(6.4).closed().make(),
            SecurityLabelExample.OKAY,
            "test_0"));
    combinedVectors.add(VECTORS);
    assertEquals(3, combinedVectors.size());
  }

}