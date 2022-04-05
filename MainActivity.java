public class MainActivity extends AppCompatActivity {

    public static final Strategy STRATEGY = Strategy.P2P_POINT_TO_POINT;
    public static final String SERVICE_ID="120001";
    long timestamp = System.currentTimeMillis();
    String strendPointId, strReceived;

    TextView conStatus, txtReceivedData;
    Button advertise, discover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        conStatus = findViewById(R.id.textView);
        txtReceivedData = findViewById(R.id.textView2);

        advertise = findViewById(R.id.button);
        advertise.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startAdvertising();
            }
        });

        discover = findViewById(R.id.button2);
        discover.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startDiscovery();
            }
        });

    }

    private void startAdvertising () {

        AdvertisingOptions advertisingOptions = new AdvertisingOptions.Builder().setStrategy(STRATEGY).build();
        conStatus.setText("Advertising...");
        Nearby.getConnectionsClient(MainActivity.this).startAdvertising("Device A", SERVICE_ID, new ConnectionLifecycleCallback() {
            @Override
            public void onConnectionInitiated(@NonNull String endPointId, @NonNull ConnectionInfo connectionInfo) {
                Nearby.getConnectionsClient(MainActivity.this).acceptConnection(endPointId, mPayloadCallback);
                conStatus.setText("Discovering Device Initiated Connection...accepting Connection");
            }
            @Override
            public void onConnectionResult(@NonNull String endPointId, @NonNull ConnectionResolution connectionResolution) {
                conStatus.setText("A:Connection RESULT AVAILABLE");
                switch (connectionResolution.getStatus().getStatusCode()) {
                    case ConnectionsStatusCodes.STATUS_OK:
                        // We're connected! Can now start sending and receiving data.
                        conStatus.setText("A:STATUS_OK");
                        strendPointId = endPointId;
                        sendPayLoad(strendPointId);
                        break;
                    case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                        // The connection was rejected by one or both sides.
                        conStatus.setText("A:CONNECTION REJECTED");
                        break;
                    case ConnectionsStatusCodes.STATUS_ERROR:
                        // The connection broke before it was able to be accepted.
                        conStatus.setText("A:CONNECTION ERROR");
                        break;
                    default:
                        // Unknown status code
                        conStatus.setText("A:Advertising Stopped...");
                }
            }
            @Override
            public void onDisconnected(@NonNull String s) {
                strendPointId = null;
            }
        }, advertisingOptions);


    }

    private void startDiscovery() {
        conStatus.setText("Discovery Started...");
        DiscoveryOptions discoveryOptions = new DiscoveryOptions.Builder().setStrategy(STRATEGY).build();
        Nearby.getConnectionsClient(MainActivity.this).startDiscovery(SERVICE_ID, new EndpointDiscoveryCallback() {
                    @Override
                    public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
                        conStatus.setText("Advertising Device Found");
                        Nearby.getConnectionsClient(MainActivity.this).requestConnection("Device B", endpointId, new ConnectionLifecycleCallback() {
                                    @Override
                                    public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
                                        Nearby.getConnectionsClient(MainActivity.this).acceptConnection(endpointId, mPayloadCallback);
                                        conStatus.setText("Advertising Device Initiated Connection...Accepting Connection");
                                    }
                                    @Override
                                    public void onConnectionResult(@NonNull String s, @NonNull ConnectionResolution connectionResolution) {
                                        conStatus.setText("D:Connection RESULT AVAILABLE");
                                        switch (connectionResolution.getStatus().getStatusCode()) {
                                            case ConnectionsStatusCodes.STATUS_OK:
                                                // We're connected! Can now start sending and receiving data.
                                                conStatus.setText("D:STATUS_OK");
                                                break;
                                            case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                                                // The connection was rejected by one or both sides.
                                                conStatus.setText("D:CONNECTION REJECTED");
                                                break;
                                            case ConnectionsStatusCodes.STATUS_ERROR:
                                                // The connection broke before it was able to be accepted.
                                                conStatus.setText("D:CONNECTION ERROR");
                                                break;
                                            default:
                                                // Unknown status code
                                                conStatus.setText("D:Discovery Stopped...");
                                        }
                                    }
                                    @Override
                                    public void onDisconnected(@NonNull String s) {
                                    }
                                });
                    }
                    @Override
                    public void onEndpointLost(@NonNull String s) {
                        // disconnected
                    }
                }, discoveryOptions);

    }

    private void sendPayLoad(final String endPointId) {
        Payload bytesPayload = Payload.fromBytes(String.valueOf(timestamp).getBytes());
        Nearby.getConnectionsClient(MainActivity.this).sendPayload(endPointId, bytesPayload).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    private final PayloadCallback mPayloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
            final   byte[] receivedBytes = payload.asBytes();
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    long timestamp = System.currentTimeMillis();
                    long timeTaken=(timestamp-Long.valueOf(new String(receivedBytes)))/1000;
                    strReceived=strReceived+"\n"+timeTaken +""+" sec"+"\n";
                    txtReceivedData.setText(strReceived+"");
                }
            });
        }
        @Override
        public void onPayloadTransferUpdate(@NonNull String s,
                                            @NonNull PayloadTransferUpdate payloadTransferUpdate) {
            if (payloadTransferUpdate.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                // Do something with is here...
            }
        }
    };

}
