package com.refinitiv.ema.examples.rrtmdviewer.desktop.discovered_endpoint;

import com.refinitiv.ema.examples.rrtmdviewer.desktop.common.model.DictionaryDataModel;

import java.util.List;

public class ServiceEndpointDataModel {

    private DictionaryDataModel dictionaryData;

    private List<DiscoveredEndpointInfoModel> endpoints;

    public ServiceEndpointDataModel(DictionaryDataModel dictionaryData, List<DiscoveredEndpointInfoModel> endpoints) {
        this.dictionaryData = dictionaryData;
        this.endpoints = endpoints;
    }

    public DictionaryDataModel getDictionaryData() {
        return dictionaryData;
    }

    public void setDictionaryData(DictionaryDataModel dictionaryData) {
        this.dictionaryData = dictionaryData;
    }

    public List<DiscoveredEndpointInfoModel> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<DiscoveredEndpointInfoModel> endpoints) {
        this.endpoints = endpoints;
    }
}
