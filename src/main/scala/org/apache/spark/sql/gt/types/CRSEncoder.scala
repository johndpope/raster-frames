/*
 * Copyright 2017 Astraea, Inc.
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

package org.apache.spark.sql.gt.types

import geotrellis.proj4.CRS
import geotrellis.raster.{CellType, DataType}
import org.apache.spark.sql.catalyst.ScalaReflection
import org.apache.spark.sql.catalyst.analysis.GetColumnByOrdinal
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.spark.sql.types.{ObjectType, StringType, StructField, StructType}
import org.apache.spark.unsafe.types.UTF8String

import scala.reflect.classTag

/**
 * Custom encoder for GT [[CellType]]. It's necessary since [[CellType]] is a type alias of
 * a type intersection.
 * @author sfitch 
 * @since 7/21/17
 */
object CRSEncoder {
  def apply(): ExpressionEncoder[CRS] = {
    import org.apache.spark.sql.catalyst.expressions._
    import org.apache.spark.sql.catalyst.expressions.objects._
    val ctType = ScalaReflection.dataTypeFor[CRS]
    val schema = StructType(Seq(StructField("crsProj4", StringType, false)))
    val inputObject = BoundReference(0, ctType, nullable = false)

    val intermediateType = ObjectType(classOf[String])
    val serializer: Expression =
      StaticInvoke(classOf[UTF8String], StringType, "fromString",
        Invoke(inputObject, "toProj4String", intermediateType, Nil) :: Nil
      )

    val inputRow = GetColumnByOrdinal(0, schema)
    val deserializer: Expression =
      StaticInvoke(CRSEncoder.getClass, ctType, "fromString",
        Invoke(inputRow, "toString", intermediateType, Nil) :: Nil
      )

    ExpressionEncoder[CRS](
      schema,
      flat = false,
      Seq(serializer),
      deserializer,
      classTag[CRS]
    )
  }

  // Not sure why this delegate is necessary, but doGenCode fails without it.
  def fromString(str: String): CRS =  CRS.fromString(str)

}
