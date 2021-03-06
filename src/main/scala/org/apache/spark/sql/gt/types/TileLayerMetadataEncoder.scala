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

import geotrellis.spark.{KeyBounds, TileLayerMetadata}
import org.apache.spark.sql.Encoder
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.spark.sql.gt.Implicits._

import scala.reflect.runtime.universe._

/**
 * Specialized encoder for [[TileLayerMetadata]], necessary to be able to delegate to the
 * speciallized cell type and crs encoders.
 * @author sfitch 
 * @since 7/21/17
 */
object TileLayerMetadataEncoder extends DelegatingSubfieldEncoder {

  private def fieldEncoders = Seq[(String, ExpressionEncoder[_])](
    "cellType" -> cellTypeEncoder,
    "layout" -> layoutDefinitionEncoder,
    "extent" -> extentEncoder,
    "crs" -> crsEncoder
  )

  def apply[K: TypeTag](): Encoder[TileLayerMetadata[K]] = {
    val boundsEncoder = ExpressionEncoder[KeyBounds[K]]()
    val fEncoders = fieldEncoders :+ ("bounds" -> boundsEncoder)
    create(fEncoders)
  }
}
