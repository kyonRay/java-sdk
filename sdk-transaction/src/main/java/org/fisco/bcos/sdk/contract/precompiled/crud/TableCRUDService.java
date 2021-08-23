/*
 * Copyright 2014-2020  [fisco-dev]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package org.fisco.bcos.sdk.contract.precompiled.crud;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.contract.precompiled.callback.PrecompiledCallback;
import org.fisco.bcos.sdk.contract.precompiled.crud.common.Condition;
import org.fisco.bcos.sdk.contract.precompiled.crud.common.Entry;
import org.fisco.bcos.sdk.contract.precompiled.crud.table.TableFactory;
import org.fisco.bcos.sdk.contract.precompiled.model.PrecompiledAddress;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.PrecompiledConstant;
import org.fisco.bcos.sdk.model.PrecompiledRetCode;
import org.fisco.bcos.sdk.model.RetCode;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.transaction.codec.decode.ReceiptParser;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.utils.ObjectMapperFactory;
import org.fisco.bcos.sdk.utils.StringUtils;

public class TableCRUDService {
    private final Client client;
    private final CRUDPrecompiled crudService;
    private final TableFactory tableFactory;
    private static final String ValueFieldsDelimiter = ",";

    public TableCRUDService(Client client, CryptoKeyPair credential) {
        this.client = client;
        this.crudService =
                CRUDPrecompiled.load(
                        PrecompiledAddress.CRUD_PRECOMPILED_ADDRESS, client, credential);
        this.tableFactory =
                TableFactory.load(
                        PrecompiledAddress.TABLEFACTORY_PRECOMPILED_ADDRESS, client, credential);
    }

    public static String convertValueFieldsToString(List<String> valueFields) {
        return StringUtils.join(valueFields, ValueFieldsDelimiter);
    }

    public void checkKey(String key) throws ContractException {
        if (key.length() > PrecompiledConstant.TABLE_KEY_MAX_LENGTH) {
            throw new ContractException(PrecompiledRetCode.OVER_TABLE_KEY_LENGTH_LIMIT);
        }
    }

    public RetCode createTable(String tableName, String keyFieldName, List<String> valueFields)
            throws ContractException {
        checkKey(keyFieldName);
        String valueFieldsString = convertValueFieldsToString(valueFields);
        return ReceiptParser.parseTransactionReceipt(
                tableFactory.createTable(tableName, keyFieldName, valueFieldsString));
    }

    public RetCode insert(String tableName, Entry fieldNameToValue) throws ContractException {
        try {
            String fieldNameToValueStr =
                    ObjectMapperFactory.getObjectMapper()
                            .writeValueAsString(fieldNameToValue.getFieldNameToValue());
            return ReceiptParser.parseTransactionReceipt(
                    crudService.insert(tableName, fieldNameToValueStr, ""));
        } catch (JsonProcessingException e) {
            throw new ContractException(
                    "insert "
                            + fieldNameToValue.toString()
                            + " to "
                            + tableName
                            + " failed, error info:"
                            + e.getMessage(),
                    e);
        }
    }

    public RetCode update(String tableName, Entry fieldNameToValue, Condition condition)
            throws ContractException {
        try {
            String fieldNameToValueStr =
                    ObjectMapperFactory.getObjectMapper()
                            .writeValueAsString(fieldNameToValue.getFieldNameToValue());
            String conditionStr = encodeCondition(condition);
            return ReceiptParser.parseTransactionReceipt(
                    crudService.update(tableName, fieldNameToValueStr, conditionStr, ""));
        } catch (JsonProcessingException e) {
            throw new ContractException(
                    "update "
                            + fieldNameToValue.toString()
                            + " to "
                            + tableName
                            + " failed, error info:"
                            + e.getMessage(),
                    e);
        }
    }

    private String encodeCondition(Condition condition) throws JsonProcessingException {
        if (condition == null) {
            condition = new Condition();
        }
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(condition.getConditions());
    }

    public RetCode remove(String tableName, Condition condition) throws ContractException {
        try {
            String conditionStr = encodeCondition(condition);
            return ReceiptParser.parseTransactionReceipt(
                    crudService.remove(tableName, conditionStr, ""));
        } catch (JsonProcessingException e) {
            throw new ContractException("remove key  with condition from " + tableName + " failed");
        }
    }

    public List<Map<String, String>> select(String tableName, Condition condition)
            throws ContractException {
        try {
            String conditionStr = encodeCondition(condition);
            return parseSelectResult(crudService.select(tableName, conditionStr, ""));
        } catch (ContractException e) {
            throw ReceiptParser.parseExceptionCall(e);
        } catch (JsonProcessingException e) {
            throw new ContractException(
                    "select "
                            + " with condition from "
                            + tableName
                            + " failed, error info:"
                            + e.getMessage(),
                    e);
        }
    }

    public static List<Map<String, String>> parseSelectResult(String selectResult)
            throws JsonProcessingException {
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        return objectMapper.readValue(
                selectResult,
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
    }

    private List<Map<String, String>> getTableDesc(String tableName) throws ContractException {
        Tuple2<String, String> tableDesc = crudService.desc(tableName);
        List<Map<String, String>> tableDescList = new ArrayList<>(1);
        Map<String, String> keyToValue = new HashMap<>();
        keyToValue.put(PrecompiledConstant.KEY_FIELD_NAME, tableDesc.getValue1());
        keyToValue.put(PrecompiledConstant.VALUE_FIELD_NAME, tableDesc.getValue2());
        tableDescList.add(0, keyToValue);
        return tableDescList;
    }

    public List<Map<String, String>> desc(String tableName) throws ContractException {
        try {
            return getTableDesc(tableName);
        } catch (ContractException e) {
            throw ReceiptParser.parseExceptionCall(e);
        }
    }

    private TransactionCallback createTransactionCallback(PrecompiledCallback callback) {
        return new TransactionCallback() {
            @Override
            public void onResponse(TransactionReceipt receipt) {
                RetCode retCode = null;
                try {
                    retCode = ReceiptParser.parseTransactionReceipt(receipt);

                } catch (ContractException e) {
                    retCode.setCode(e.getErrorCode());
                    retCode.setMessage(e.getMessage());
                    retCode.setTransactionReceipt(receipt);
                }
                callback.onResponse(retCode);
            }
        };
    }

    public void asyncInsert(String tableName, Entry fieldNameToValue, PrecompiledCallback callback)
            throws ContractException {
        try {
            String fieldNameToValueStr =
                    ObjectMapperFactory.getObjectMapper()
                            .writeValueAsString(fieldNameToValue.getFieldNameToValue());
            this.crudService.insert(
                    tableName, fieldNameToValueStr, "", createTransactionCallback(callback));
        } catch (JsonProcessingException e) {
            throw new ContractException(
                    "asyncInsert "
                            + fieldNameToValue.toString()
                            + " to "
                            + tableName
                            + " failed, error info:"
                            + e.getMessage(),
                    e);
        }
    }

    public void asyncUpdate(
            String tableName,
            Entry fieldNameToValue,
            Condition condition,
            PrecompiledCallback callback)
            throws ContractException {
        try {
            String fieldNameToValueStr =
                    ObjectMapperFactory.getObjectMapper()
                            .writeValueAsString(fieldNameToValue.getFieldNameToValue());
            String conditionStr = encodeCondition(condition);
            this.crudService.update(
                    tableName,
                    fieldNameToValueStr,
                    conditionStr,
                    "",
                    createTransactionCallback(callback));
        } catch (JsonProcessingException e) {
            throw new ContractException(
                    "asyncUpdate "
                            + fieldNameToValue.toString()
                            + " to "
                            + tableName
                            + " failed, error info:"
                            + e.getMessage(),
                    e);
        }
    }

    public void asyncRemove(String tableName, Condition condition, PrecompiledCallback callback)
            throws ContractException {
        try {
            this.crudService.remove(
                    tableName, encodeCondition(condition), "", createTransactionCallback(callback));
        } catch (JsonProcessingException e) {
            throw new ContractException("asyncRemove with condition from " + tableName + " failed");
        }
    }
}
