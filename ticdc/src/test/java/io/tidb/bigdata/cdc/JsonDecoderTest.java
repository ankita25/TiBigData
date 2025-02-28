/*
 * Copyright 2021 TiDB Project Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package io.tidb.bigdata.cdc;


import static io.tidb.bigdata.cdc.FileLoader.decode;
import static io.tidb.bigdata.cdc.FileLoader.decodeValue;
import static io.tidb.bigdata.cdc.FileLoader.listFiles;

import io.tidb.bigdata.cdc.Key.Type;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

public class JsonDecoderTest {
  private final static Codec CODEC = Codec.json();

  private static long verifyDDL(final String fileName, long lastTs, final Expect.DDL[] expected)
      throws IOException {
    final EventDecoder decoder = decode(CODEC, fileName);
    final ValueDecoder valueDecoder = decodeValue(CODEC, fileName);
    int idx = 0;
    for (Event evt : decoder) {
      final Expect.DDL ddl = expected[idx++];
      Assert.assertEquals(evt.getType(), Type.DDL);
      Assert.assertTrue(evt.getTs() >= lastTs);
      Assert.assertEquals(evt.asDDL().getType(), ddl.type);
      Assert.assertEquals(evt.getSchema(), ddl.schema);
      Assert.assertEquals(evt.getTable(), ddl.table);
      Assert.assertEquals(evt.asDDL().getQuery(), ddl.query);
      lastTs = evt.getTs();
      Value value = valueDecoder.next();
      Assert.assertNotNull(value);
      Assert.assertTrue(value instanceof DDLValue);
      Assert.assertEquals(evt.getValue(), value);
    }
    Assert.assertEquals(idx, expected.length);
    return lastTs;
  }

  private static long verifyResolved(final String fileName, long lastTs) throws IOException {
    final EventDecoder decoder = decode(CODEC, fileName);
    final ValueDecoder valueDecoder = decodeValue(CODEC, fileName);
    for (Event evt : decoder) {
      Assert.assertEquals(evt.getType(), Type.RESOLVED);
      Assert.assertTrue(evt.getTs() >= lastTs);
      Assert.assertNull(evt.getSchema());
      Assert.assertNull(evt.getTable());
      lastTs = evt.getTs();
      final Value value = valueDecoder.next();
      Assert.assertNotNull(value);
      Assert.assertTrue(value instanceof ResolvedValue);
      Assert.assertEquals(evt.getValue(), value);
    }
    return lastTs;
  }

  private static long verifyRowChange(final String fileName, long lastTs,
      final Expect.RowChange[] expected)
      throws IOException {
    final EventDecoder decoder = decode(CODEC, fileName);
    final ValueDecoder valueDecoder = decodeValue(CODEC, fileName);
    int idx = 0;
    for (Event evt : decoder) {
      Assert.assertEquals(evt.getType(), Type.ROW_CHANGED);
      Assert.assertTrue(evt.getTs() >= lastTs);
      lastTs = evt.getTs();
      final Value value = valueDecoder.next();
      Assert.assertNotNull(value);
      Assert.assertTrue(value instanceof RowChangedValue);
      Assert.assertEquals(evt.getValue(), value);
      idx++;
    }
    Assert.assertEquals(idx, expected.length);
    return lastTs;
  }

  @Test
  public void testDecodeAll() throws IOException {
    final File[] keyTests = listFiles(CODEC, "key");
    final File[] valueTests = listFiles(CODEC, "value");
    Assert.assertNotNull(keyTests);
    Assert.assertNotNull(valueTests);
    Arrays.sort(keyTests);
    Arrays.sort(valueTests);
    Assert.assertArrayEquals(
        Arrays.stream(keyTests).map(File::getName).toArray(),
        Arrays.stream(valueTests).map(File::getName).toArray());

    for (int idx = 0, length = keyTests.length; idx < length; ++idx) {
      final EventDecoder decoder = decode(CODEC, keyTests[idx], valueTests[idx]);
      for (final Event event : decoder) {
        Assert.assertNotNull(event);
      }
    }
  }

  @Test
  public void testDDL() throws IOException {
    long lastTs = verifyDDL("ddl_0", -1,
        new Expect.DDL[]{
            new Expect.DDL(
                DDLValue.Type.CREATE_SCHEMA,
                "a",
                null,
                "create database a;")});
    lastTs = verifyDDL("ddl_1", lastTs,
        new Expect.DDL[]{
            new Expect.DDL(
                DDLValue.Type.CREATE_SCHEMA,
                "a",
                null,
                "create database a;"),
            new Expect.DDL(
                DDLValue.Type.DROP_SCHEMA,
                "a",
                null,
                "drop database a;"),
            new Expect.DDL(
                DDLValue.Type.CREATE_SCHEMA,
                "a",
                null,
                "create database a;"),
            new Expect.DDL(
                DDLValue.Type.CREATE_TABLE,
                "a",
                "c",
                "create table c(id int primary key);"),
        });
    verifyDDL("ddl_2", lastTs, new Expect.DDL[0]);
  }

  @Test
  public void testResolved() throws IOException {
    long lastTs = verifyResolved("rts_0", -1);
    lastTs = verifyResolved("rts_1", lastTs);
    verifyResolved("rts_2", lastTs);
  }

  @Test
  public void testRowChanged() throws IOException {
    long lastTs = verifyRowChange("row_0", -1, new Expect.RowChange[]{
        new Expect.RowChange(RowChangedValue.Type.INSERT, "a", "b"),
    });
    lastTs = verifyRowChange("row_1", lastTs, new Expect.RowChange[]{
        new Expect.RowChange(RowChangedValue.Type.INSERT, "a", "b"),
        new Expect.RowChange(RowChangedValue.Type.INSERT, "a", "b"),
        new Expect.RowChange(RowChangedValue.Type.INSERT, "a", "b"),
        new Expect.RowChange(RowChangedValue.Type.INSERT, "a", "c"),
    });
    verifyRowChange("row_2", lastTs, new Expect.RowChange[]{});
  }

}
