public class MainActivity extends AppCompatActivity {

    //Some variables that are used in program
    Button advertise, discover;
    TextView  txtReceivedData;
    String strReceived, strendPointId;


    //STRATEGY and SERVICE_ID initialized
    public static final Strategy STRATEGY = Strategy.P2P_POINT_TO_POINT;
    public static final String SERVICE_ID="120001";


    //PayloadCallback Object Created
    private final PayloadCallback mPayloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
            final byte[] receivedBytes = payload.asBytes();
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    long timestamp = System.currentTimeMillis();
                    long timeTaken = (timestamp - Long.valueOf(new String(receivedBytes))) / 1000;
                    strReceived = strReceived + "\n" + timeTaken + "" + " sec" + "\n";
                    txtReceivedData.setText(strReceived + "");
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



    //onCreate method Starts here
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //layout variable initializations
        advertise = findViewById(R.id.button);
        discover = findViewById(R.id.button2);

        txtReceivedData = findViewById(R.id.textView3);


        //On Click Listener applied on advertise button
        advertise.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startAdvertising();
            }
        });

        //OnClick Listener applied on discover button.
        discover.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startDiscovery();
            }
        });


        //onCreate Method Ends Here.
    }


    //startAdvertising method Definition.
    private void startAdvertising () {
        AdvertisingOptions advertisingOptions = new AdvertisingOptions.Builder().setStrategy(STRATEGY).build();
        Nearby.getConnectionsClient(MainActivity.this).startAdvertising("Device A", SERVICE_ID, new ConnectionLifecycleCallback() {
            @Override
            public void onConnectionInitiated(@NonNull String endPointId, @NonNull ConnectionInfo connectionInfo) {
                Nearby.getConnectionsClient(MainActivity.this).acceptConnection(endPointId, mPayloadCallback);
            }
            @Override
            public void onConnectionResult(@NonNull String endPointId, @NonNull ConnectionResolution connectionResolution) {
                switch (connectionResolution.getStatus().getStatusCode()) {
                    case ConnectionsStatusCodes.STATUS_OK:
                        // We're connected! Can now start sending and receiving data.

                        strendPointId = endPointId;
                        sendPayLoad(strendPointId);
                        break;
                    case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                        // The connection was rejected by one or both sides.
                        break;
                    case ConnectionsStatusCodes.STATUS_ERROR:
                        // The connection broke before it was able to be accepted.
                        break;
                    default:
                        // Unknown status code
                }
            }
            @Override
            public void onDisconnected(@NonNull String s) {
                strendPointId = null;

            }
        }, advertisingOptions);

    }



    //startDiscovery method Definition.
    private void startDiscovery() {
        DiscoveryOptions discoveryOptions = new DiscoveryOptions.Builder().setStrategy(STRATEGY).build();
        Nearby.getConnectionsClient(MainActivity.this).
                startDiscovery(SERVICE_ID, new EndpointDiscoveryCallback() {
                    @Override
                    public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
                        Nearby.getConnectionsClient(MainActivity.this).
                                requestConnection("Device B", endpointId, new ConnectionLifecycleCallback() {
                                    @Override
                                    public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
                                        Nearby.getConnectionsClient(MainActivity.this).acceptConnection(endpointId, mPayloadCallback);
                                    }
                                    @Override
                                    public void onConnectionResult(@NonNull String s, @NonNull ConnectionResolution connectionResolution) {
                                        switch (connectionResolution.getStatus().getStatusCode()) {
                                            case ConnectionsStatusCodes.STATUS_OK:
                                                // We're connected! Can now start sending and receiving data.
                                                break;
                                            case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                                                // The connection was rejected by one or both sides.
                                                break;
                                            case ConnectionsStatusCodes.STATUS_ERROR:
                                                // The connection broke before it was able to be accepted.
                                                break;
                                            default:
                                                // Unknown status code
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




    //sendPayload Method Definition.
    private void sendPayLoad(final String endPointId) {
        long timestamp = System.currentTimeMillis();
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


    //MainActivity class ends here
}
