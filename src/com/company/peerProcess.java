import java.io.*;
import java.util.*;
import java.net.*;
import java.time.Instant;
import java.util.concurrent.*;

class ChooseNode {
    int nodepty;
    int index;

    public ChooseNode(int nodepty, int index) {
        this.nodepty = nodepty;
        this.index = index;
    }
}


class SetDownloadRates {
    String p_id;
    Integer downloadSpeed;
    public SetDownloadRates(String p_id, Integer downloadSpeed) {
        this.p_id = p_id;
        this.downloadSpeed = downloadSpeed;
    }
}


class Configuration {

    static int noOfPreffNeighbors;
    static int unChoking_interval;
    static int optUnChoking_interval;
    static int total_peices;
    static String file_name;
    static int piece_size;
    static int file_size;

    public Configuration() {
        try
        {
            InputStream is = new FileInputStream("C:\\Users\\varsh\\CN_project\\src\\com\\company\\Common.cfg");
            Properties prp = new Properties();
            prp.load(is);
            noOfPreffNeighbors  = Integer.parseInt(prp.getProperty("NumberOfPreferredNeighbors"));
            unChoking_interval  = Integer.parseInt(prp.getProperty("UnchokingInterval"));
            optUnChoking_interval        = Integer.parseInt(prp.getProperty("OptimisticUnchokingInterval"));
            file_name                    = prp.getProperty("FileName");
            file_size                    = Integer.parseInt(prp.getProperty("FileSize"));
            piece_size                   = Integer.parseInt(prp.getProperty("PieceSize"));

            if(file_size%piece_size == 0){
                total_peices = file_size/piece_size;
            } else {
                total_peices = (file_size/piece_size) + 1;
            }
        }
        catch (Exception ex)
        {
            System.out.println("Error occured in config File: "+ex);
        }
    }

}

class PeerNode {
    private int file_exists;
    private String host_id;
    private int port_num;
    private String peer_id;

    public PeerNode(String peer_id, String host_id, int port_num, int file_exists) {
        this.peer_id = peer_id;
        this.host_id = host_id;
        this.port_num = port_num;
        this.file_exists = file_exists;
    }
    public void setPeerId(String peer_id) {
        this.peer_id = peer_id;
    }
    public String getPeerId() {
        return peer_id;
    }
    public void setHostId(String host_id) {
        this.host_id = host_id;
    }
    public String getHostId() {
        return host_id;
    }
    public void setPortNum(int port_num) {
        this.port_num = port_num;
    }
    public int getFileExists() {
        return file_exists;
    }
    public int getPortNum() {
        return port_num;
    }
    public void setFileExists(int file_exists) {
        this.file_exists = file_exists;
    }
}


class PieceMsg extends OriginalMsg {
    int msgType;
    byte[] peice;

    public PieceMsg(int msgType, byte[] peice) {
        this.msgType = msgType;
        this.peice = peice;
    }

    public byte[] getPiece() {
        return peice;
    }

    public void setPiece(byte[] peice) {
        this.peice = peice;
    }
}

class ConnectionDataDetails extends Thread {

    private String p_id;
    private Socket skt;

    private ObjectOutputStream out;
    private ObjectInputStream in_res;

    protected Queue<Object> msg_Queue;
    private Peer local_peer;
    private boolean is_handShakeCompleted;
    private MsgClassProcessor msg_Cntlr;

    public MsgClassProcessor retrieveMH() {
        return msg_Cntlr;
    }

    public void MQ_Setter(Queue<Object> msg_Queue) {
        this.msg_Queue = msg_Queue;
    }

    public int getLength() {
        return msg_Queue.size();
    }

    public Queue<Object> retrieve_MQ() {
        return msg_Queue;
    }

    public ConnectionDataDetails(String p_id, Socket skt, ObjectOutputStream out, ObjectInputStream in_res, Peer local_peer, Queue<Object> msg_Queue) {
        this.p_id = p_id;
        this.out = out;
        this.skt = skt;
        this.local_peer = local_peer;
        this.in_res = in_res;
        this.msg_Queue = msg_Queue;

    }


    public void setPeerId(String p_id) {
        this.p_id = p_id;
    }

    public String getPeerId() {
        return p_id;
    }


    public void setSocket(Socket skt) {
        this.skt = skt;
    }
    public Socket getSocket() {
        return skt;
    }

    synchronized public ObjectOutputStream getOut() {
        return out;
    }
    public ObjectInputStream getIn() {
        return in_res;
    }


    public void setOut(ObjectOutputStream out) {
        this.out = out;
    }
    public void setIn(ObjectInputStream in_res) {
        this.in_res = in_res;
    }


    public boolean is_handShakeCompleted() {
        return is_handShakeCompleted;
    }

    public void setHandShakeDone(boolean handShakeDone) {
        this.is_handShakeCompleted = handShakeDone;
    }

    public void run() {
        try
        {
            this.msg_Cntlr = new MsgClassProcessor(this.local_peer, this, this.msg_Queue);
            msg_Cntlr.start();
            while (true) {
                Object mesage = this.in_res.readObject();
                msg_Queue.add(mesage);
            }
        }
        
       catch (Exception ex)
        {
            System.out.println("Exception: "+ex);
        }
    }

    synchronized public void send(Object mesage) throws IOException {
        if (mesage instanceof HandshakeClass) {
            System.out.println("sending the handshake message to " + p_id);
        } else if (mesage instanceof OriginalMsg) {
            if (((OriginalMsg) mesage).getMessageType() == 7)
                System.out.println("Message: " + ((OriginalMsg) mesage).getMessagePayload().split("-")[0]);
            else
                System.out.println("Message: " + ((OriginalMsg) mesage).getMessagePayload());
        }
        out.writeObject(mesage);
        out.flush();
    }
}


class HandshakeClass implements Serializable{
    private String header = "P2PFILESHARINGPROJ";
    private byte[] z_b = new byte[10];
    private String peer_id;
    private static final long serialVersionUID = 42L;

    public HandshakeClass() {}

    public HandshakeClass(String peer_id) {
        this.peer_id = peer_id;
    }

    public String getHeader() {
        return header;
    }

    public void hdr_setter(String header) {
        this.header = header;
    }

    public void z_b_setter(byte[] z_b) {
        this.z_b = z_b;
    }

    public byte[] retrieve_z_b() {
        return z_b;
    }


    public String getPeerId() {
        return peer_id;
    }

    public void setPeerId(String peer_id) {
        this.peer_id = peer_id;
    }
}

class MsgClassProcessor extends Thread{
    Peer peer_data;
    ConnectionDataDetails connInfo;
    Queue<Object> msg_Queue;
    LinkedList<Integer> peices = new LinkedList<>();
    Thread unChokeThread;
    int lReq;

    public Thread uncokThread() {
        return unChokeThread;

    }

    public MsgClassProcessor(Peer peer_data, ConnectionDataDetails connInfo, Queue<Object> msg_Queue) {

        this.peer_data = peer_data;
        this.connInfo = connInfo;
        this.msg_Queue = msg_Queue;
    }

    public void HSprocessor(HandshakeClass handshakeObj) throws IOException {
        Map<String, String> hs_status = peer_data.retrieveHSVal();
        Map<String, String> bf_status = peer_data.retrieveBFVal();
        boolean piece_exists = peer_data.piece_exists();
        if(hs_status.getOrDefault(connInfo.getPeerId(), "False").compareTo("False") == 0) {
            connInfo.setPeerId(handshakeObj.getPeerId());
            peer_data. retrieveP_to_PConn().put(handshakeObj.getPeerId(), connInfo);
            if(handshakeObj.getHeader().compareTo("P2PFILESHARINGPROJ") == 0) {
                connInfo.send(new HandshakeClass(peer_data.getPeerId()));
                if(piece_exists) {
                    connInfo.send(new OriginalMsg(5, String.valueOf(peer_data.retreive_BF())));
                    bf_status.put(connInfo.getPeerId(), "Sent");
                }
            }
        }
        else if(hs_status.getOrDefault(connInfo.getPeerId(), "False").compareTo("Sent") == 0) {
            if(handshakeObj.getHeader().compareTo("P2PFILESHARINGPROJ") == 0 && handshakeObj.getPeerId().compareTo(connInfo.getPeerId()) == 0) {
                if(piece_exists) {
                    connInfo.send(new OriginalMsg(5, String.valueOf(peer_data.retreive_BF())));

                    bf_status.put(connInfo.getPeerId(), "Sent");
                }
            }
        }
        hs_status.put(connInfo.getPeerId(), "True");
        peer_data.setHSVal(hs_status);
        peer_data.BFValSetter(bf_status);
        peer_data. retrieveDownloadRt().put(connInfo.getPeerId(), 0);
    }

    public void run() {
        System.out.println("Running ");
        while(true) {
            while(msg_Queue.size() != 0) {
                Object mesage = msg_Queue.poll();
                if(mesage instanceof HandshakeClass) {
                    try {
                        peer_data.log_Writer("[" + Instant.now()+"]: Peer [" + peer_data.getPeerId()+"] makes a connection to Peer [" + ((HandshakeClass) mesage).getPeerId() + "]");
                        HSprocessor((HandshakeClass) mesage);
                    } catch (IOException ex) {
                        System.out.println("Exception in processing message : "+ex);

                    }
                }
                else if (mesage instanceof  PieceMsg) {
                    PieceMsg pieceMessage = (PieceMsg) mesage;
                    if(pieceMessage.getMessageType() == 7)
                    try {
                        peer_data.log_Writer("["+ Instant.now() + "]: Peer  [" + peer_data.getPeerId() +"]  has downloaded the piece " + lReq + " from " + connInfo.getPeerId() +".Now 
the number of pieces it has is "+(peer_data.getPeiceAtIndex().size() + 1));
                        pieceMessageControl(pieceMessage);
                    } catch (IOException e) {
                        System.out.println("Exception in writing log: "+e);
                    }
                    break;
                }
                else if (mesage instanceof OriginalMsg){
                    OriginalMsg actMsg = (OriginalMsg) mesage;
                    if(actMsg.getMessageType() == 0 ){
                        try {
                            peer_data.log_Writer("["+ Instant.now() + "]: Peer [" + peer_data.getPeerId() +"] is chockedd by [" + connInfo.getPeerId() +"]");
                            chokeMessageControl((OriginalMsg) mesage);
                        } catch (IOException ex) {
                            System.out.println("IO Exception: "+ex);
                        }
                    }else if(actMsg.getMessageType() == 1){
                        try {
                            peer_data.log_Writer("["+ Instant.now() + "]: Peer [" + peer_data.getPeerId() +"] is unchoked by [" + connInfo.getPeerId() +"]");
                            unchokeMessageControl((OriginalMsg) mesage);
                        } catch (IOException e) {
                            System.out.println("Exception: "+e);
                        }
                    }else if (actMsg.getMessageType() == 2){
                        try {
                            peer_data.log_Writer("["+ Instant.now() + "]: Peer [" + peer_data.getPeerId() +"] received the ‘interested’ message from [" + connInfo.getPeerId() +"]" );
                        } catch (IOException e) {
                            System.out.println("Exception: "+e);
                        }
                    }else if(actMsg.getMessageType() == 3){ 
                        try {
                            peer_data.log_Writer("["+ Instant.now() + "]: Peer " + peer_data.getPeerId() +" received the ‘not interested’ message from [" + connInfo.getPeerId() +"]");
                        } catch (IOException e) {
                            System.out.println("Exception: "+e);
                        }
                    }else if(actMsg.getMessageType() == 4){
                        try {
                            peer_data.log_Writer("["+ Instant.now() + "]: Peer [" + peer_data.getPeerId() +"] received the  ‘have’ message from [" + connInfo.getPeerId() +"] for the peice: " + ((OriginalMsg) mesage).getMessagePayload());
                            Msgexists((OriginalMsg) mesage);
                        } catch (IOException e) {
                            System.out.println("Exception: "+e);
                        }
                    }else if (actMsg.getMessageType() == 5){
                        try {
                            bitFieldMessageControl((OriginalMsg) mesage);
                        } catch (IOException e) {
                            System.out.println("Exception: "+e);
                        }
                    }else if (actMsg.getMessageType() == 6){
                        try {
                            ReqmsgControl((OriginalMsg) mesage);
                        } catch (IOException e) {
                            System.out.println("Exception: "+e);
                        }
                    }else if (actMsg.getMessageType() == 7){
                        try {
                            peer_data.log_Writer("["+ Instant.now() + "]: Peer [" + peer_data.getPeerId() +"] has downloaded the peice " + lReq + " from [" + connInfo.getPeerId() +"]. Now 
the number of pieces it has is "+(peer_data.getPeiceAtIndex().size() + 1));
                        } catch (IOException e) {
                            System.out.println("Exception: "+e);
                        }
                    }
                }
            }
        }
    }


    public void chokeMessageControl(OriginalMsg mesage) {
        peer_data.getCkdPr().add(connInfo.getPeerId());
        peer_data.unCkd_prs().remove(connInfo.getPeerId());
    }


    public void unchokeMessageControl(OriginalMsg mesage) throws IOException, InterruptedException {
        if(unChokeThread == null) {
            unChokeThread = new Thread(new Runnable() {
                public void run() {
                    try {

                        String p_id = connInfo.getPeerId();
                        Set<Integer> reqed = peer_data.reqg_t();
                        Collections.shuffle(peices);
                        while(true) {
                            while(!peer_data.getCkdPr().contains(p_id) && !peices.isEmpty()) { 
                                int peice = peices.remove();
                                if(peer_data.retreive_BF()[peice] == '0' && !reqed.contains(peice)) {
                                    connInfo.send(new OriginalMsg(6, Integer.toString(peice)));
                                    reqed.add(peice);
                                    lReq = peice;
                                    while(reqed.contains(peice)) {
                                        if(peer_data.getCkdPr().contains(p_id)) {
                                            reqed.remove(peice);
                                            peices.add(peice);
                                            break;
                                        }
                                    }
                                    Thread.sleep(55);
                                }
                                
                            }
                            while(!peer_data.unCkd_prs().contains(p_id)) {
                                Thread.sleep(100);
                            }

                        }
                    } catch (IOException e) {
                        System.out.println("Exception: "+e);
                    }

                }
            });
            unChokeThread.start();
        }
        peer_data.getCkdPr().remove(connInfo.getPeerId());
        peer_data.unCkd_prs().add(connInfo.getPeerId());
    }


    public void notInterestedMessageControl(OriginalMsg mesage) {
        peer_data.retrieve_Interested_Key().remove(connInfo.getPeerId());
    }

    public void interestedMessageControl(OriginalMsg mesage) {
        peer_data.retrieve_Interested_Key().add(connInfo.getPeerId());
    }

    public void Msgexists(OriginalMsg actMsg) throws IOException {
        int peice = Integer.parseInt(actMsg.getMessagePayload());
        char[] b_f = peer_data.retreive_BF();
        if(b_f[peice] == '0') {
            peices.add(peice);
            connInfo.send(new OriginalMsg(2, null));
        }
        ConcurrentMap<String, String> map = peer_data.retrieve_BM_Val();
        String bitFieldOfReceivedPeer = map.get(connInfo.getPeerId());
        if(bitFieldOfReceivedPeer == null) {
            char[] defaultBitField = new char[Configuration.total_peices];
            Arrays.fill(defaultBitField, '0');
            bitFieldOfReceivedPeer = String.valueOf(defaultBitField);
        }
        char[] ch = bitFieldOfReceivedPeer.toCharArray();
        ch[peice] = '1';
        map.put(connInfo.getPeerId(), String.valueOf(ch));

    }



    public void ReqmsgControl(OriginalMsg actMsg) throws IOException {
        if(!peer_data.retrieveChokedKey().contains(connInfo.getPeerId())) {
            int reqPc = Integer.parseInt(actMsg.getMessagePayload());
            byte[] peice = peer_data.getPeiceAtIndex().get(reqPc);
            connInfo.send(new PieceMsg(7, peice));
            peer_data. retrieveDownloadRt().put(connInfo.getPeerId(), peer_data. retrieveDownloadRt().getOrDefault(connInfo.getPeerId(), 0) + 1);
        }
    }
    public void bitFieldMessageControl(OriginalMsg actMsg) throws IOException {
        Map<String, String> bf_status = peer_data.retrieveBFVal();
        if(bf_status.getOrDefault(connInfo.getPeerId(), "False").compareTo("False") == 0) {
            if(peer_data.piece_exists()) {
                connInfo.send(new OriginalMsg(5, String.valueOf(peer_data.retreive_BF())));
            }
            else{
                connInfo.send(new OriginalMsg(5, String.valueOf(peer_data.retreive_BF())));
            }
        }
        String BitFMsg = actMsg.getMessagePayload();
        String bt_fd = String.valueOf(peer_data.retreive_BF());
        peer_data.retrieve_BM_Val().put(connInfo.getPeerId(), BitFMsg);
        for(int i = 0; i < BitFMsg.length(); i++) {
            if(BitFMsg.charAt(i) == '1' && bt_fd.charAt(i) == '0')
                peices.add(i);
        }

        for(int i = 0; i < bt_fd.length(); i++) {
            if(BitFMsg.charAt(i) == '1' && BitFMsg.charAt(i) != bt_fd.charAt(i)) {
                connInfo.send(new OriginalMsg(2));
                return;
            }
        }
        connInfo.send(new OriginalMsg(3));
    }

    public void pieceMessageControl(PieceMsg actMsg) throws IOException {

        int pcIdx = lReq;
        char[] ch = peer_data.retreive_BF();
        ch[pcIdx] = '1';
        peer_data.BF_Setter(ch);
        peer_data.getPeiceAtIndex().put(pcIdx, actMsg.getPiece());
        peer_data.reqg_t().remove(pcIdx);

        for(String peer_id : peer_data. retrieveP_to_PConn().keySet()) {
            ConnectionDataDetails connInfo = peer_data. retrieveP_to_PConn().get(peer_id);
            String bt_fd = peer_data.retrieve_BM_Val().get(peer_id);
            connInfo.send(new OriginalMsg(4, Integer.toString(pcIdx)));

        }

        if(peer_data.getPeiceAtIndex().size() == Configuration.total_peices) {
            for(String peer_id : peer_data. retrieveP_to_PConn().keySet()) {
                ConnectionDataDetails connInfo = peer_data. retrieveP_to_PConn().get(peer_id);
                connInfo.send(new OriginalMsg(3, null));
            }
            peer_data.log_Writer("["+ Instant.now() + "]: Peer [" + peer_data.getPeerId() +"] has downloaded  complete file");
            return;
        }

        ConcurrentMap<String, String> map = peer_data.retrieve_BM_Val();
        String bitFieldOfReceivedPeer = map.get(connInfo.getPeerId());
        char[] myBitField = peer_data.retreive_BF();
        boolean hasPcStr = false;
        for(int i = 0; i < bitFieldOfReceivedPeer.length(); i++) {
            if(myBitField[i] == '0' && bitFieldOfReceivedPeer.charAt(i) == '1') {
                hasPcStr = true;
                break;
            }
        }
        if(!hasPcStr)
            connInfo.send(new OriginalMsg(3));
    }
}

class Peer {
    Configuration config;
    String peer_id;
    char[] b_f = new char[Configuration.total_peices];
    String hostName;
    int port_num;
    Srvercls svr;
    List<ConnectionDataDetails> connList = new ArrayList<>();
    List<PeerNode> tot_peers = new ArrayList<>();

    ConcurrentMap<String, String> BtFMap = new ConcurrentHashMap<>();

    List<PeerNode> connection_prs = new ArrayList<>();
    Set<String> accepted = Collections.synchronizedSet(new HashSet<>());
    Set<String> unCkd = Collections.synchronizedSet(new HashSet<>());

    Set<String> Ckd = Collections.synchronizedSet(new HashSet<>());
    ConcurrentMap<String, Integer> downloadSpeed = new ConcurrentHashMap<>();
    ConcurrentMap<String, ConnectionDataDetails> prConnect = new ConcurrentHashMap<>();

    Set<String> prfNbr = Collections.synchronizedSet(new HashSet<>());
    Set<String> rstNeighbourselect = Collections.synchronizedSet(new HashSet<>());
    ConcurrentMap<Integer, byte[]> ele_index = new ConcurrentHashMap<>();

    PriorityBlockingQueue<ChooseNode> selectionNodes;
    Set<Integer> reqed = Collections.synchronizedSet(new HashSet<>());

    FileWriter fw;
    BufferedWriter bw;
    boolean initFileexists;

    Set<String> unCkdfrPr = Collections.synchronizedSet(new HashSet<>());
    Set<String> ckdFrPr = Collections.synchronizedSet(new HashSet<>());

    public Set<String> unCkd_prs() {
        return unCkdfrPr;
    }

    public void setUnckd_prs(Set<String> unCkdfrPr) {
        this.unCkdfrPr = unCkdfrPr;
    }

    public Set<String> getCkdPr() {
        return ckdFrPr;
    }

    public void setCkd_prs(Set<String> ckdFrPr) {
        this.ckdFrPr = ckdFrPr;
    }

    public boolean initFileExistsG_t() {
        return initFileexists;
    }

    public void initFileExists_st(boolean initFileexists) {
        this.initFileexists = initFileexists;
    }

    public Set<Integer> reqg_t() {
        return reqed;
    }

    public void reqs_t(Set<Integer> reqed) {
        this.reqed = reqed;
    }

    public PriorityBlockingQueue<ChooseNode> SelectG_t() {
        return selectionNodes;
    }

    public void Selects_t(PriorityBlockingQueue<ChooseNode> selectionNodes) {
        this.selectionNodes = selectionNodes;
    }

    public ConcurrentMap<Integer, byte[]> getPeiceAtIndex() {
        return ele_index;
    }

    public void setPieceAtIndex(ConcurrentMap<Integer, byte[]> ele_index) {
        this.ele_index = ele_index;
    }

    public Set<String> BestNbrg_t() {
        return rstNeighbourselect;
    }

    public void BestNbrs_t(Set<String> rstNeighbourselect) {
        this.rstNeighbourselect = rstNeighbourselect;
    }

    public void setConfig(Configuration config) {
        this.config = config;
    }

    public void setConnections(List<ConnectionDataDetails> connList) {
        this.connList = connList;
    }

    public void setAllPeers(List<PeerNode> tot_peers) {
        this.tot_peers = tot_peers;
    }

    public void BitFields_t(ConcurrentMap<String, String> BtFMap) {
        this.BtFMap = BtFMap;
    }

    public void setPeersToConnect(List<PeerNode> connection_prs) {
        this.connection_prs = connection_prs;
    }

    public void unChokeKeySetter(Set<String> unCkd) {
        this.unCkd = unCkd;
    }

    public void  chokeKeySetter(Set<String> Ckd) {
        this.Ckd = Ckd;
    }

    public void PeerToConnSetter(ConcurrentMap<String, ConnectionDataDetails> prConnect) {
        this.prConnect = prConnect;
    }

    public void  downloadRSetter(ConcurrentMap<String, Integer> downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }

    public Set<String>  retrievePrefNeighbours() {
        return prfNbr;
    }

    public void   prefNeighbourSetter(Set<String> prfNbr) {
        this.prfNbr = prfNbr;
    }

    public void  interestedKeySetter(Set<String> accepted) {
        this.accepted = accepted;
    }

    public ConcurrentMap<String, ConnectionDataDetails>  retrieveP_to_PConn() {
        return prConnect;
    }

    public ConcurrentMap<String, Integer>  retrieveDownloadRt() {
        return downloadSpeed;
    }

    public Set<String>  unChokedKeySetter() {
        return unCkd;
    }

    public Set<String> retrieveChokedKey() {
        return Ckd;
    }

    volatile Queue<Elem_MQ> msg_Queue = new LinkedList<>();
    boolean verifySelectedNeighbours;
    MsgClassProcessor msg_Cntlr;
    Map<String, String> hs_status = new HashMap<>();
    Map<String, String> bf_status = new HashMap<>();

    public Map<String, String> retrieveBFVal() {
        return bf_status;
    }

    public void BFValSetter(Map<String, String> bf_status) {
        this.bf_status = bf_status;
    }

    public Map<String, String> retrieveHSVal() {
        return hs_status;
    }

    public void setHSVal(Map<String, String> hs_status) {
        this.hs_status = hs_status;
    }

    public MsgClassProcessor retrieveMH() {
        return msg_Cntlr;
    }

    public void MHSetter(MsgClassProcessor msg_Cntlr) {
        this.msg_Cntlr = msg_Cntlr;
    }

    public boolean verifySelectedNeighbours() {
        return verifySelectedNeighbours;
    }

    public void SNSetter(boolean selectingNeighbors) {
        verifySelectedNeighbours = selectingNeighbors;
    }

    public Queue<Elem_MQ> retrieve_MQ() {
        return msg_Queue;
    }

    public void MQ_Setter(Queue<Elem_MQ> msg_Queue) {
        this.msg_Queue = msg_Queue;
    }

    public List<ConnectionDataDetails> retrieve_Conn() {
        return connList;
    }

    public Set<String> retrieve_Interested_Key() {
        return accepted;
    }

    public List<PeerNode> retrieve_P2Conn() {
        return connection_prs;
    }

    public Configuration retrieve_Config() {
        return config;
    }

    public Peer(String peer_id, Configuration config) throws IOException {
        this.peer_id = peer_id;
        this.config = config;
        this.fw = new FileWriter("log_peer_[" + peer_id +"].log");
        this.bw = new BufferedWriter(fw);
    }

    public FileWriter retrieve_FileWriter() {
        return fw;
    }

    public void fileWriter_Key_Setter(FileWriter fw) {
        this.fw = fw;
    }

    public BufferedWriter getBw() {
        return bw;
    }

    public void setBw(BufferedWriter bw) {
        this.bw = bw;
    }

    public void log_Writer(String mesage) throws IOException {
        synchronized (fw) {
            this.bw.write(mesage);
            this.bw.newLine();
        }
    }

    public ConcurrentMap<String, String> retrieve_BM_Val() {
        return BtFMap;
    }

    public List<PeerNode> retrieve_Peers() {
        return tot_peers;
    }

    public Srvercls retrieve_Srv() {
        return svr;
    }

    public void Srv_Setter(Srvercls svr) {
        this.svr = svr;
    }

    public String retrieve_HN() {
        return hostName;
    }

    public void HN_Setter(String hostName) {
        this.hostName = hostName;
    }

    public int getPortNum() {
        return port_num;
    }

    public void setPortNum(int port_num) {
        this.port_num = port_num;
    }

    public String getPeerId() {
        return peer_id;
    }

    public void setPeerId(String peer_id) {
        this.peer_id = peer_id;
    }

    public char[] retreive_BF() {
        return b_f;
    }

    synchronized public void BF_Setter(char[] b_f) {
        this.b_f = b_f;
    }

    public Peer() {
    }

    boolean piece_exists() {
        char[] bt_fd = this.retreive_BF();
        for(int i = 0; i < bt_fd.length; i++){
            if(bt_fd[i] == '1')
                return true;
        }
        return false;
    }

    public void retrieve_Peer_Details() throws FileNotFoundException {
        BufferedReader b_R = new BufferedReader(new FileReader("C:\\Users\\varsh\\CN_project\\src\\com\\company\\PeerInfo.cfg"));
        String line = null;
        try {
            boolean is_P2P_Conn = true;
            while((line = b_R.readLine()) != null) {
                String[] str_split = line.split(" ");
                PeerNode p_Det = new PeerNode(str_split[0], str_split[1], Integer.parseInt(str_split[2]), Integer.parseInt(str_split[3]));
                if(p_Det.getPeerId().compareTo(this.peer_id) == 0) {
                    p_Det_Setter(p_Det);
                    is_P2P_Conn = false;
                }
                if(is_P2P_Conn) {
                    connection_prs.add(p_Det);
                }
                tot_peers.add(p_Det);
                Ckd.add(p_Det.getPeerId());
            }
        }catch (FileNotFoundException ex1) {
            System.out.println("PeerInfo.cfg not found:" +ex1);
        }catch (IOException ex2) {
            System.out.println("IOException: " +ex2);
        }finally {
            try {
                b_R.close();
            } catch (IOException e) {
                System.out.println("Exception: " +e);
            }
        }
    }

    public void p_Det_Setter(PeerNode p_Det) {
        char[] b_f = new char[Configuration.total_peices];
        if(p_Det.getFileExists() == 1) {
            Arrays.fill(b_f, '1');
        }
        else {
            Arrays.fill(b_f, '0');
        }
        initFileExists_st(p_Det.getFileExists() == 1);
        BF_Setter(b_f);
        setPortNum(p_Det.getPortNum());
        HN_Setter(p_Det.getHostId());
    }
}

public class peerProcess {
    static Peer peer_data;
    static String peer_id;


    public static void main(String[] args) throws Exception {
        peer_id = "1002";
        if(args.length != 0)
            peer_id = args[0];
        peer_data = new Peer(peer_id, new Configuration());
        peer_data.retrieve_Config();
        peer_data.retrieve_Peer_Details();
        pecSelectAlgo();
        if(peer_data.initFileExistsG_t())
            SptFile();
        Srvercls svr = new Srvercls(peer_data.getPortNum(), peer_data);
        peer_data.Srv_Setter(svr);
        svr.start();


        for(PeerNode peerInfoServer : peer_data.retrieve_P2Conn()) {
            try {
                Socket reqSocket = new Socket(peerInfoServer.getHostId(), peerInfoServer.getPortNum());
                ObjectOutputStream out = new ObjectOutputStream(reqSocket.getOutputStream());
                ObjectInputStream in_res = new ObjectInputStream(reqSocket.getInputStream());
                HandshakeClass handshakeObj = new HandshakeClass(peer_data.getPeerId());
                out.writeObject(handshakeObj);
                peer_data.log_Writer("["+ Instant.now() + "]: Peer: [" + peer_id +"] makes a connection to Peer: [" + peerInfoServer.getPeerId()+"]");
                Map<String, String> hs_status = peer_data.retrieveHSVal();
                hs_status.put(peerInfoServer.getPeerId(), "Sent"); //updating
                peer_data.setHSVal(hs_status);
                ConnectionDataDetails connInfo = new ConnectionDataDetails(peerInfoServer.getPeerId(), reqSocket, out, in_res, peer_data, new ConcurrentLinkedQueue<Object>());
                peer_data.retrieve_Conn().add(connInfo);
                peer_data. retrieveP_to_PConn().put(peerInfoServer.getPeerId(), connInfo);
                connInfo.start();
            } catch (IOException e) {
                System.out.println("Exception "+e);
            }
        }
        timeIt();
        System.exit(0);
    }

    public static void timeIt() throws InterruptedException {
        Thread checkTime = new Thread(new Runnable() {
            @Override
            public void run() {
                final ScheduledExecutorService preferredNeighborsScheduler = Executors.newScheduledThreadPool(1);
                final ScheduledExecutorService optimisticNeighborScheduler = Executors.newScheduledThreadPool(1);
                Runnable prfNbr = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            msgChocked();
                            getNbrMsg();
                            msgUnchoked();
                            StringBuilder strObj = new StringBuilder("["+ Instant.now() + "]: Peer [" + peer_id +"] has the preferred neighbors [");
                            for(String s : peer_data. retrievePrefNeighbours()) {
                                strObj.append(s + ", ");
                            }
                            strObj.append("]");
                            peer_data.log_Writer(strObj.toString());
                        } catch (IOException e) {
                            System.out.println("Exception: " +e);
                        }
                    }
                };
                Runnable rstNeighbourselect = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            getBestNbr();
                            StringBuilder strObj = new StringBuilder("["+ Instant.now() + "]: Peer [" + peer_id +"] has the optimistically unchoked neighbor [");
                            for(String s : peer_data.BestNbrg_t()) {
                                strObj.append(s + ", ");
                            }
                            strObj.append("]");
                            peer_data.log_Writer(strObj.toString());
                        } catch (IOException e) {
                            System.out.println("Exception: "+e);
                        }
                    }
                };
                final ScheduledFuture<?> PNH = preferredNeighborsScheduler.scheduleAtFixedRate(prfNbr, 0, Configuration.unChoking_interval, TimeUnit.SECONDS);
                final ScheduledFuture<?> ONH = optimisticNeighborScheduler.scheduleAtFixedRate(rstNeighbourselect, 1, Configuration.optUnChoking_interval, TimeUnit.SECONDS);

                while(true) {
                    try {
                        Thread.sleep(3000);
                        if(completedBool()) {
                            PNH.cancel(true);
                            ONH.cancel(true);
                            break;
                        }
                    } catch (InterruptedException e) {
                        System.out.println("Interrupted Exception:" +e);
                    } catch (IOException e) {
                        System.out.println("IOException:" +e);
                    }
                }
            }
        });

        while(peer_data.retrieve_Interested_Key().size() == 0) {
            Thread.sleep(1000);
        }
        checkTime.start();
        while(checkTime.isAlive()) {
            Thread.sleep(1000);
        }
    }

    public static void msgChocked() throws IOException {
        Set<String> unCkd = peer_data. unChokedKeySetter();
        Set<String> Ckd = peer_data.retrieveChokedKey();
        Map<String, ConnectionDataDetails> prConnect = peer_data. retrieveP_to_PConn();
        Set<String> prfNbr = peer_data. retrievePrefNeighbours();

        for(String peer_id : prfNbr) {
            ConnectionDataDetails connInfo = prConnect.get(peer_id);
            connInfo.send(new OriginalMsg(0, null));
            Ckd.add(peer_id);
            unCkd.remove(peer_id);
        }

        prfNbr.clear();
        peer_data.prefNeighbourSetter(prfNbr);
        peer_data.unChokeKeySetter(unCkd);
        peer_data.chokeKeySetter(Ckd);
    }
    public static void msgUnchoked() throws IOException {
        Map<String, ConnectionDataDetails> prConnect = peer_data. retrieveP_to_PConn();

        for(String p_id : peer_data.retrievePrefNeighbours()) {
            ConnectionDataDetails connInfo = prConnect.get(p_id);
            connInfo.send(new OriginalMsg(1, null));
        }

    }

    public static void getNbrMsg() throws IOException {
        Set<String> Ckd = peer_data.retrieveChokedKey();
        Set<String> unCkd = peer_data.unChokedKeySetter();
        Set<String> accepted = peer_data.retrieve_Interested_Key();
        Set<String> prfNbr = new HashSet<>();
        ConcurrentMap<String, Integer> downloadSpeed = peer_data.retrieveDownloadRt();
        PriorityQueue<SetDownloadRates> priorityQ = new PriorityQueue<SetDownloadRates>((a, b) -> b.downloadSpeed - a.downloadSpeed);

        for(String key : downloadSpeed.keySet()) {
            priorityQ.add(new SetDownloadRates(key, downloadSpeed.get(key)));

            downloadSpeed.put(key, 0);
        }

        int prefNbrNum = Configuration.noOfPreffNeighbors;
        while(prefNbrNum > 0 && priorityQ.size() != 0) {
            SetDownloadRates node = priorityQ.poll();
            if(accepted.contains(node.p_id)) {
                unCkd.add(node.p_id);
                Ckd.remove(node.p_id);
                prfNbr.add(node.p_id);
                prefNbrNum--;
            }
        }

        peer_data.prefNeighbourSetter(prfNbr);
        peer_data.unChokeKeySetter(unCkd);
        peer_data.chokeKeySetter(Ckd);
        peer_data.interestedKeySetter(accepted);
        peer_data.downloadRSetter(downloadSpeed);
    }



    public static void getBestNbr() throws IOException {

        Set<String> rstNeighbourselect = peer_data.BestNbrg_t();
        Set<String> accepted = peer_data.retrieve_Interested_Key();
        Set<String> Ckd = peer_data.retrieveChokedKey();
        Set<String> unCkd = peer_data.unChokedKeySetter();
        List<String> interestedAndChoked = new ArrayList<>();
        Map<String, ConnectionDataDetails> prConnect = peer_data.retrieveP_to_PConn();

        for(String p_id : rstNeighbourselect) {
            ConnectionDataDetails connInfo = prConnect.get(p_id);
            connInfo.send(new OriginalMsg(0, null));
            rstNeighbourselect.remove(p_id);
            Ckd.add(p_id);
        }

        for(String p_id : accepted) {
            if(Ckd.contains(p_id)) {
                interestedAndChoked.add(p_id);
            }
        }

        Collections.shuffle(interestedAndChoked);

        if(interestedAndChoked.size() != 0) {
            String p_id = interestedAndChoked.get(0);
            ConnectionDataDetails connInfo = prConnect.get(p_id);
            connInfo.send(new OriginalMsg(1, null));
            unCkd.add(p_id);
            Ckd.remove(p_id);
            rstNeighbourselect.add(p_id);
        }

        peer_data.chokeKeySetter(Ckd);
        peer_data.unChokeKeySetter(unCkd);
        peer_data.interestedKeySetter(accepted);
        peer_data.BestNbrs_t(rstNeighbourselect);
    }

    public static void SptFile() throws IOException {
        File fls = new File("C:\\Users\\varsh\\CN_project\\src\\com\\company\\thefile");
        FileInputStream inStream = new FileInputStream(fls);
        BufferedInputStream bufferStream = new BufferedInputStream(inStream);

        byte[] elements;
        long lenFs = fls.length();
        long curr = 0;
        int index = 0;

        ConcurrentMap<Integer, byte[]> ele_index = new ConcurrentHashMap<>();

        while(curr < lenFs){
            int size = Configuration.piece_size;
            if(lenFs - curr >= size)
                curr += size;
            else{
                size = (int)(lenFs - curr);
                curr = lenFs;
            }
            elements = new byte[size];
            bufferStream.read(elements, 0, size);
            ele_index.put(index, elements);
            index++;
        }

        peer_data.setPieceAtIndex(ele_index);
    }

    public static void join_all_files() throws IOException {
        Map<Integer, byte[]> ele_index = peer_data.getPeiceAtIndex();
        int peices = Configuration.total_peices;
        FileOutputStream outStream = new FileOutputStream(Configuration.file_name);
        for(int i = 0; i < peices; i++) {
            outStream.write(ele_index.get(i));
        }
    }

    public static void pecSelectAlgo() {

        int p_id = Integer.parseInt(peer_data.getPeerId());
        int numPrs = peer_data.retrieve_Peers().size();
        int totalRng = (int) Math.ceil(Configuration.total_peices / numPrs);
        int min = Math.max(0, (p_id % 1000 - 1) * totalRng);
        int max = Math.min(min + totalRng, Configuration.total_peices);

        PriorityBlockingQueue<ChooseNode> priorityQ = new PriorityBlockingQueue<ChooseNode>(10, (a, b) -> b.nodepty - a.nodepty);

        List<ChooseNode> lst = new ArrayList<>();

        for(int i = 0; i < Configuration.total_peices; i++) {
            if(i >= min && i <= max) {
                lst.add(new ChooseNode(2, i));
            }
            else {
                lst.add(new ChooseNode(1, i));
            }
        }

        Collections.shuffle(lst);

        for(int i = 0; i < lst.size(); i++) {
            priorityQ.add(lst.get(i));
        }

        peer_data.Selects_t(priorityQ);

    }

    public static boolean completedBool() throws InterruptedException, IOException {
        List<PeerNode> tot_peers = peer_data.retrieve_Peers();
        int N = 0;
        for(PeerNode Pr1 : tot_peers) {
            String bt_fd;
            if(Pr1.getPeerId().compareTo(peer_id) == 0) {
                bt_fd = String.valueOf(peer_data.retreive_BF());
            }
            else {
                bt_fd = peer_data.retrieve_BM_Val().getOrDefault(Pr1.getPeerId(), null);
            }
            if(bt_fd == null)
                continue;
            for(int i = 0; i < bt_fd.length(); i++) {
                if(bt_fd.charAt(i) == '0')
                    return false;
            }
            N++;
        }
        if(N == 3) {
            Thread.sleep(10000);
            exitAll();
            crtFs();
            return true;
        }
        return false;
    }

    public static void crtFs() throws IOException {
        File fls2 = new File("./peer_" + peer_id);
        if (!fls2.exists()){
            fls2.mkdirs();
        }
        ConcurrentMap<Integer, byte[]> peices = peer_data.getPeiceAtIndex();
        File fls = new File("./peer_" + peer_id + "/" + "thefile");
        FileOutputStream stm = new FileOutputStream(fls);
        for(int i = 0; i < Configuration.total_peices; i++) {
            stm.write(peices.get(i));
        }
        stm.close();
    }

    public static void exitAll() throws IOException, InterruptedException {
        ConcurrentMap<String, ConnectionDataDetails> connList = peer_data.retrieveP_to_PConn();

        for(String peer_data : connList.keySet()) {
            ConnectionDataDetails conn2 = connList.get(peer_data);
            Thread th1 = (Thread) conn2;
            Thread th2 = (Thread) conn2.retrieveMH();
            Thread unChoke = (Thread) conn2.retrieveMH().uncokThread();
            if(unChoke != null) {
                unChoke.stop();
            }
            Thread.sleep(1000);
            if(th2 != null) {
                th2.stop();
            }

            if(th1 != null) {
                th1.stop();
            }
        }
        Thread svr = (Thread) peer_data.retrieve_Srv();
        svr.stop();
    }
}

class OriginalMsg implements Serializable {
    private int msg_size;
    private int msgType;
    private String msgPayload;
    private static final long serialVersionUID = 8983558202217591746L;

    public OriginalMsg() {}

    public OriginalMsg(int msgType, String msgPayload) {
        this.msgType = msgType;
        this.msgPayload = msgPayload;
        if(msgPayload != null)
            this.msg_size = msgPayload.length() + 1;
        else
            this.msg_size = 1;
    }

    public OriginalMsg(int msgType) {
        this.msgType = msgType;
        this.msg_size = 1;
    }

    public int MSG_len_gt() {
        return msg_size;
    }

    public void MSG_len_st(int msg_size) {
        this.msg_size = msg_size;
    }

    public int getMessageType() {
        return msgType;
    }

    public void MSG_ty_st(int msgType) {
        this.msgType = msgType;
    }

    public String getMessagePayload() {
        return msgPayload;
    }

    public void setMessagePayload(String msgPayload) {
        this.msgPayload = msgPayload;
    }
}

class Elem_MQ {
    ConnectionDataDetails connInfo;
    Object mesage;

    public Elem_MQ(ConnectionDataDetails connInfo, Object mesage) {
        this.connInfo = connInfo;
        this.mesage = mesage;
    }

    public ConnectionDataDetails retrieve_Conn_Det() {
        return connInfo;
    }

    public void Con_Det_Setter(ConnectionDataDetails connInfo) {
        this.connInfo = connInfo;
    }

    public Object retrieve_Msg() {
        return mesage;
    }

    public void Msg_Setter(Object mesage) {
        this.mesage = mesage;
    }
}

class Srvercls extends Thread{

    private static int sPort;
    Peer peer_data;

    public Srvercls(int port_num, Peer peer_data) {
        sPort = port_num;
        this.peer_data = peer_data;
    }

    public void run() {
        ServerSocket listener = null;
        try {
            listener = new ServerSocket(sPort);
            int clientNum = 1;
            while(true) {
                Socket skt = listener.accept();
                ObjectOutputStream out = new ObjectOutputStream(skt.getOutputStream());
                out.flush();
                ObjectInputStream in_res = new ObjectInputStream(skt.getInputStream());
                String peerIdConnected = Integer.toString(clientNum);
                ConnectionDataDetails connInfo = new ConnectionDataDetails(peerIdConnected, skt, out, in_res, peer_data, new ConcurrentLinkedQueue<Object>());
                peer_data.retrieve_Conn().add(connInfo);

                connInfo.start();
                clientNum++;
            }
        } catch (IOException e) {
            System.out.println("Exception:"+e);
        }
        try {

        } finally {
            try {
                listener.close();
            } catch (IOException e) {
                System.out.println("Exception:"+e);
            }
        }
    }
}

