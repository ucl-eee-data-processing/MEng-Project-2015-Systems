/*
 * Copyright (c) 2015, Cloudera, Inc. All Rights Reserved.
 *
 * Cloudera, Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"). You may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */

package com.cloudera.oryx.lambda;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Function that deletes old data, if applicable, at each batch interval.
 *
 * @param <T> unused
 */
public final class DeleteOldDataFn<T> implements Function<JavaRDD<T>,Void> {

  private static final Logger log = LoggerFactory.getLogger(DeleteOldDataFn.class);

  private static final Pattern DATA_SUBDIR_PATTERN = Pattern.compile("-(\\d+)\\.");

  private final Configuration hadoopConf;
  private final String dataDirString;
  private final int maxDataAgeHours;

  public DeleteOldDataFn(Configuration hadoopConf, String dataDirString, int maxDataAgeHours) {
    this.hadoopConf = hadoopConf;
    this.dataDirString = dataDirString;
    this.maxDataAgeHours = maxDataAgeHours;
  }

  @Override
  public Void call(JavaRDD<T> ignored) throws IOException {
    FileSystem fs = FileSystem.get(hadoopConf);
    FileStatus[] inputPathStatuses = fs.globStatus(new Path(dataDirString + "/*"));
    if (inputPathStatuses != null) {
      long oldestTimeAllowed =
          System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(maxDataAgeHours, TimeUnit.HOURS);
      for (FileStatus status : inputPathStatuses) {
        if (status.isDirectory()) {
          Path subdir = status.getPath();
          Matcher m = DATA_SUBDIR_PATTERN.matcher(subdir.getName());
          if (m.find() && Long.parseLong(m.group(1)) < oldestTimeAllowed) {
            log.info("Deleting old data at {}", subdir);
            try {
              fs.delete(subdir, true);
            } catch (IOException e) {
              log.warn("Unable to delete {}; continuing", subdir, e);
            }
          }
        }
      }
    }
    return null;
  }

}
