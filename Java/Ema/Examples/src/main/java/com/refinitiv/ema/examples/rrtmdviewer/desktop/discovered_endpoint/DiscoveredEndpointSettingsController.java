package com.refinitiv.ema.examples.rrtmdviewer.desktop.discovered_endpoint;

import com.refinitiv.ema.examples.rrtmdviewer.desktop.SceneController;
import com.refinitiv.ema.examples.rrtmdviewer.desktop.common.*;
import com.refinitiv.ema.examples.rrtmdviewer.desktop.common.fxcomponents.DictionaryLoaderComponent;
import com.refinitiv.ema.examples.rrtmdviewer.desktop.common.fxcomponents.ErrorDebugAreaComponent;
import com.refinitiv.ema.examples.rrtmdviewer.desktop.common.fxcomponents.PasswordEyeComponent;
import com.refinitiv.ema.examples.rrtmdviewer.desktop.common.model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

public class DiscoveredEndpointSettingsController {

    private static final String BUTTON_CONNECT_TEXT = "CONNECT";

    private static final String DEFAULT_TOKEN_SERVICE_URL = "https://api.refinitiv.com/auth/oauth2/v1/token";

    private static final String DEFAULT_SERVICE_ENDPOINT_URL = "https://api.refinitiv.com/streaming/pricing/v1";

    private static final String CLIENT_DISCOVERED_ENDPOINT_SETTINGS_ERROR = "Discovered Endpoint Settings - Validation Failure:";

    private static final String SERVICE_ENDPOINT_SELECTION_EMPTY = "Validation failure: you should select at least one available " +
            "service discovery endpoint";

    private static final String EMPTY_VALIDATION_POSTFIX = " is empty.";

    @FXML
    private TextField clientIdTextField;

    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordEyeComponent usernamePasswordComponent;

    @FXML
    private ComboBox<DiscoveredEndpointConnectionTypes> connectionTypesComboBox;

    @FXML
    private Pane encryptionOptionsPane;

    @FXML
    private CheckBox encryptionOptionCheckbox;

    @FXML
    private TextField keyFileTextFld;

    @FXML
    private PasswordEyeComponent keyPasswordComponent;

    @FXML
    private Pane customServiceUrlsPane;

    @FXML
    private CheckBox customServiceUrlsCheckbox;

    @FXML
    private TextField tokenServiceUrl;

    @FXML
    private TextField serviceDiscoveryUrl;

    @FXML
    private Pane useProxyPane;

    @FXML
    private CheckBox useProxyCheckbox;

    @FXML
    private TextField proxyHostTextFld;

    @FXML
    private TextField proxyPortTextFld;

    @FXML
    private Pane useProxyAuthenticationPane;

    @FXML
    private CheckBox useProxyAuthenticationCheckbox;

    @FXML
    private TextField proxyAuthLogin;

    @FXML
    private PasswordEyeComponent proxyAuthPassword;

    @FXML
    private TextField proxyAuthDomain;

    @FXML
    private TextField krbFileTextFld;

    @FXML
    private Button primaryActionButton;

    @FXML
    private ListView<DiscoveredEndpointInfoModel> serviceEndpointChoiceBox;

    @FXML
    private VBox serviceEndpointVBox;

    @FXML
    private TabPane primaryTabPane;

    @FXML
    private ErrorDebugAreaComponent errorDebugArea;

    @FXML
    private DictionaryLoaderComponent dictionaryLoader;

    private final OMMViewerError ommViewerError = new OMMViewerError();
    private AsyncResponseModel asyncResponseObserver;

    private ExecutorService executorService;
    private SceneController sceneController;
    private DiscoveredEndpointSettingsService discoveredEndpointSettingsService;
    private DiscoveredEndpointSettingsModel discoveredEndpointSettings;
    private final ServiceEndpointDataModel serviceEndpointData = new ServiceEndpointDataModel();

    private boolean serviceEndpointsRetrieved;

    @FXML
    public void initialize() {
        sceneController = ApplicationSingletonContainer.getBean(SceneController.class);
        executorService = ApplicationSingletonContainer.getBean(ExecutorService.class);
        tokenServiceUrl.setText(DiscoveredEndpointSettingsModel.DEFAULT_TOKEN_SERVICE_URL);
        serviceDiscoveryUrl.setText(DiscoveredEndpointSettingsModel.DEFAULT_TOKEN_SERVICE_URL);
        discoveredEndpointSettingsService = ApplicationSingletonContainer.getBean(DiscoveredEndpointSettingsService.class);

        //Prepare ListView selection
        serviceEndpointChoiceBox.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        serviceEndpointChoiceBox.addEventFilter(MouseEvent.MOUSE_PRESSED, this::handleEndpointSelection);
    }

    @FXML
    public void handleSubmitBtnAction(ActionEvent actionEvent) {
        primaryActionButton.setDisable(true);
        validateFields();
        if (ommViewerError.isFailed()) {
            errorDebugArea.writeError(ommViewerError.toString());
            primaryActionButton.setDisable(false);
            return;
        }
        if (!serviceEndpointsRetrieved) {
            discoveredEndpointSettings = mapDiscoveredEndpointSettings();
            primaryTabPane.setDisable(true);

            /* Create and register an observer for every requests */
            asyncResponseObserver = new AsyncResponseModel();
            prepareAsyncResponseObserver();

            executorService.submit(new FxRunnableTask(() -> discoveredEndpointSettingsService
                    .requestServiceDiscovery(discoveredEndpointSettings, asyncResponseObserver)));
        } else {
            final FxRunnableTask fxRunnableTask = new FxRunnableTask(
                    () -> discoveredEndpointSettingsService.createOmmConsumer(
                            discoveredEndpointSettings, serviceEndpointData, ommViewerError)
            );
            fxRunnableTask.setOnSucceeded(e -> {
                if (!ommViewerError.isFailed()) {
                    errorDebugArea.stopDebugStreaming();
                    sceneController.addScene(ApplicationLayouts.ITEM_VIEW);
                    sceneController.showLayout(ApplicationLayouts.ITEM_VIEW);
                } else {
                    errorDebugArea.writeError(ommViewerError.toString());
                    primaryActionButton.setDisable(false);
                }
            });
            executorService.submit(fxRunnableTask);
        }
    }

    public void handleEndpointSelection(MouseEvent event) {
        Node node = event.getPickResult().getIntersectedNode();

        while (node != null && node != serviceEndpointChoiceBox && !(node instanceof ListCell)) {
            node = node.getParent();
        }

        if (node instanceof ListCell<?>) {
            event.consume(); //suspend further execution of the event

            ListCell<?> cell = (ListCell<?>) node;
            int index = cell.getIndex();
            DiscoveredEndpointInfoModel discoveredInfo = serviceEndpointChoiceBox.getItems().get(index);
            if (serviceEndpointChoiceBox.getSelectionModel().isSelected(index)) {
                serviceEndpointChoiceBox.getSelectionModel().clearSelection(index);
                serviceEndpointData.getEndpoints().remove(discoveredInfo);
            } else {
                serviceEndpointChoiceBox.getSelectionModel().select(index);
                serviceEndpointData.getEndpoints().add(discoveredInfo);
            }
        }
    }

    @FXML
    public void handleEncryptionOptionsCheckbox(ActionEvent event) {
        encryptionOptionsPane.setDisable(!encryptionOptionCheckbox.isSelected());
    }

    @FXML
    public void handleCustomServiceUrlsCheckbox(ActionEvent event) {
        customServiceUrlsPane.setDisable(!customServiceUrlsCheckbox.isSelected());
    }

    @FXML
    public void handleUseProxyCheckbox(ActionEvent event) {
        useProxyPane.setDisable(!useProxyCheckbox.isSelected());
        useProxyAuthenticationPane.setDisable(!useProxyCheckbox.isSelected() || !useProxyAuthenticationCheckbox.isSelected());
        useProxyAuthenticationCheckbox.setDisable(!useProxyCheckbox.isSelected());
    }

    @FXML
    public void handleUseProxyAuthenticationCheckbox(ActionEvent event) {
        useProxyAuthenticationPane.setDisable(!useProxyAuthenticationCheckbox.isSelected());
    }

    @FXML
    public void handleKeyFileChooserButton(ActionEvent event) {
        File keyFile = sceneController.showFileChooserWindow();
        if (keyFile != null) {
            keyFileTextFld.setText(keyFile.getAbsolutePath());
        }
    }

    @FXML
    public void handleKrbFileChooserButton(ActionEvent event) {
        File krbFile = sceneController.showFileChooserWindow();
        if (krbFile != null) {
            krbFileTextFld.setText(krbFile.getAbsolutePath());
        }
    }

    @FXML
    public void handleConnectionTypeComboBox(ActionEvent event) {
        DiscoveredEndpointConnectionTypes connectionType = connectionTypesComboBox.getValue();
        final boolean isWebSocket = Objects.equals(DiscoveredEndpointConnectionTypes.ENCRYPTED_WEBSOCKET, connectionType);
        dictionaryLoader.getDownloadDictCheckbox().setSelected(!isWebSocket);
        dictionaryLoader.onDownloadDictionaryChanged(event);
    }

    private void prepareAsyncResponseObserver() {
        asyncResponseObserver.responseStatusProperty().addListener(
                (observable, oldValue, newValue) -> onServiceDiscoveryRetrieved()
        );
    }

    private void onServiceDiscoveryRetrieved() {
        if (Objects.equals(AsyncResponseStatuses.SUCCESS, this.asyncResponseObserver.getResponseStatus())) {
            final ServiceEndpointResponseModel serviceEndpointResponseModel = (ServiceEndpointResponseModel) this.asyncResponseObserver.getResponse();
            final List<DiscoveredEndpointInfoModel> info = serviceEndpointResponseModel.getResponseData();
            this.serviceEndpointData.setDictionaryData(this.dictionaryLoader.createDictionaryModel());
            serviceEndpointsRetrieved = true;
            Platform.runLater(() -> {
                if (!info.isEmpty()) {
                    primaryActionButton.setText(BUTTON_CONNECT_TEXT);
                    primaryActionButton.getStyleClass().add("connect-button");
                    serviceEndpointVBox.setDisable(false);
                    serviceEndpointChoiceBox.setItems(FXCollections.observableList(info));
                }
                primaryActionButton.setDisable(false);
            });
        } else if (Objects.equals(AsyncResponseStatuses.FAILED, this.asyncResponseObserver.getResponseStatus())) {
            final String msg = this.asyncResponseObserver.getResponse().toString();
            Platform.runLater(() -> {
                errorDebugArea.writeError(msg);
                primaryTabPane.setDisable(false);
                primaryActionButton.setDisable(false);
            });
        }
    }

    private DiscoveredEndpointSettingsModel mapDiscoveredEndpointSettings() {
        EncryptionDataModel encryptionDataModel = null;
        ProxyDataModel proxyDataModel = null;
        String tokenServiceUrl = DEFAULT_TOKEN_SERVICE_URL;
        String serviceDiscoveryUrl = DEFAULT_SERVICE_ENDPOINT_URL;
        if (encryptionOptionCheckbox.isSelected()) {
            encryptionDataModel = EncryptionDataModel.builder()
                    .keyFilePath(keyFileTextFld.getText().trim())
                    .keyPassword(keyPasswordComponent.getPasswordField().getText().trim())
                    .build();

        }

        if (useProxyCheckbox.isSelected()) {
            ProxyAuthenticationDataModel proxyAuthentication = null;
            if (useProxyAuthenticationCheckbox.isSelected()) {
                proxyAuthentication = ProxyAuthenticationDataModel.builder()
                        .login(proxyAuthLogin.getText().trim())
                        .password(proxyAuthPassword.getPasswordField().getText().trim())
                        .domain(proxyAuthDomain.getText().trim())
                        .krbFilePath(krbFileTextFld.getText().trim())
                        .build();
            }
            proxyDataModel = ProxyDataModel.builder()
                    .host(proxyHostTextFld.getText().trim())
                    .port(proxyPortTextFld.getText().trim())
                    .useProxyAuthentication(proxyAuthentication)
                    .build();
        }

        if (customServiceUrlsCheckbox.isSelected()) {
            tokenServiceUrl = this.tokenServiceUrl.getText().trim();
            serviceDiscoveryUrl = this.serviceDiscoveryUrl.getText().trim();
        }

        return DiscoveredEndpointSettingsModel.builder()
                .clientId(clientIdTextField.getText().trim())
                .username(usernameTextField.getText().trim())
                .password(usernamePasswordComponent.getPasswordField().getText().trim())
                .connectionType(connectionTypesComboBox.getValue())
                .useEncryption(encryptionDataModel)
                .useProxy(proxyDataModel)
                .tokenServiceUrl(tokenServiceUrl)
                .serviceEndpointUrl(serviceDiscoveryUrl)
                .build();
    }

    private void validateFields() {
        ommViewerError.clear();
        if (serviceEndpointsRetrieved) {
            if (serviceEndpointChoiceBox.getSelectionModel().isEmpty()) {
                ommViewerError.setFailed(true);
                ommViewerError.appendErrorText(SERVICE_ENDPOINT_SELECTION_EMPTY);
            }
        } else {
            ommViewerError.appendErrorText(CLIENT_DISCOVERED_ENDPOINT_SETTINGS_ERROR);
            boolean hasError = hasErrorField("Username", usernameTextField)
                    | hasErrorField("Password", usernamePasswordComponent.getPasswordField())
                    | hasErrorField("Client ID", clientIdTextField);
            if (encryptionOptionCheckbox.isSelected()) {
                hasError |= hasErrorField("Key File", keyFileTextFld)
                        | hasErrorField("Key Password", keyPasswordComponent.getPasswordField());
            }

            if (customServiceUrlsCheckbox.isSelected()) {
                hasError |= hasErrorField("Service Discovery URL", serviceDiscoveryUrl)
                        | hasErrorField("Token Service URL", tokenServiceUrl);
            }

            if (useProxyCheckbox.isSelected()) {
                hasError |= hasErrorField("Proxy host", proxyPortTextFld)
                        | hasErrorField("Proxy port", proxyPortTextFld);
            }

            hasError |= dictionaryLoader.validate(ommViewerError);
            ommViewerError.setFailed(hasError);
        }
    }

    private boolean hasErrorField(String name, TextInputControl field) {
        if (Objects.isNull(field.getText()) || field.getText().trim().isEmpty()) {
            ommViewerError.appendErrorText(name + EMPTY_VALIDATION_POSTFIX);
            return true;
        }
        return false;
    }
}
