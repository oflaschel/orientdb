/*
 * Copyright 2010-2012 Luca Garulli (l.garulli--at--orientechnologies.com)
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
package com.orientechnologies.orient.core.sql;

import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.command.OCommandDistributedReplicateRequest;
import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.command.OCommandRequestText;
import com.orientechnologies.orient.core.db.record.ODatabaseRecord;
import com.orientechnologies.orient.core.exception.OCommandExecutionException;

import java.util.Map;

/**
 * SQL CREATE CLUSTER command: Creates a new cluster.
 * 
 * @author Luca Garulli
 * 
 */
@SuppressWarnings("unchecked")
public class OCommandExecutorSQLCreateCluster extends OCommandExecutorSQLAbstract implements OCommandDistributedReplicateRequest {
  public static final String KEYWORD_CREATE      = "CREATE";
  public static final String KEYWORD_CLUSTER     = "CLUSTER";
  public static final String KEYWORD_ID          = "ID";

  private String             clusterName;
  private int                requestedId         = -1;

  public OCommandExecutorSQLCreateCluster parse(final OCommandRequest iRequest) {
    final ODatabaseRecord database = getDatabase();

    init((OCommandRequestText) iRequest);

    parserRequiredKeyword(KEYWORD_CREATE);
    parserRequiredKeyword(KEYWORD_CLUSTER);

    clusterName = parserRequiredWord(false);
    if (!clusterName.isEmpty() && Character.isDigit(clusterName.charAt(0)))
      throw new IllegalArgumentException("Cluster name cannot begin with a digit");

    String temp = parseOptionalWord(true);

    while (temp != null) {
      if (temp.equals(KEYWORD_ID)) {
        requestedId = Integer.parseInt(parserRequiredWord(false));
      }

      temp = parseOptionalWord(true);
      if (parserIsEnded())
        break;
    }

    final int clusterId = database.getStorage().getClusterIdByName(clusterName);
    if (clusterId > -1)
      throw new OCommandSQLParsingException("Cluster '" + clusterName + "' already exists");

    return this;
  }

  /**
   * Execute the CREATE CLUSTER.
   */
  public Object execute(final Map<Object, Object> iArgs) {
    if (clusterName == null)
      throw new OCommandExecutionException("Cannot execute the command because it has not been parsed yet");

    final ODatabaseRecord database = getDatabase();

    if (requestedId == -1) {
      return database.addCluster(clusterName);
    } else {
      return database.addCluster(clusterName, requestedId, null);
    }
  }

  @Override
  public String getSyntax() {
    return "CREATE CLUSTER <name> [ID <requested cluster id>]";
  }
}
