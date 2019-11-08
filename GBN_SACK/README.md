# Reliable Trannsport Protocol Implementing an Optimized GO-Back-N with Selective ACKs
Based on the modeling environment provided _NetworkSimulator_, the optimized GBN with SACK protocol is able to provide reliable transport between two network entities, a sender and a receiver. The GBN-SACK protocol is able to capture useful statistics including actual lost/corrupt rate, numer of retransimissions, number of valid receiving packets on both sender and receiver side, as well as effective average RTT, average communication cost for packets, throughput, and goodput. The main design idea of GBN-SACK is to reduce packet retransmissions in a vanilla GBN. It is achieved by allowing a small buffer at receiver side, buffering some packets sent from the  send window of a sender. During the acking process, the receiver not only acks back its cumulative ack but also some sequence number of packets buffered to signal what to retranmit in future. My design of GBN-SACK protocol in an implementation level can be divided into several classes, which are buffering, send window, checksum, sender & receiver behaviors, and statistics.

1. **Buffer** <`Buffer.java`>

    In the reliable transport protocol, Buffer is used by communicating hosts to buffer incoming packets that needs to send or unordered packets for reducing redundant retransmissions especially in GBN protocol. `Buffer.java` is a generic class implemented by resizeable ArrayList and provides API to stores packets sent and to be sent. Buffered packets are sorted by the time they were buffered. When resizing needed, it can truncate previously sent packets to allow more packets come in.

2. **Send Window** <`SendWindow.java`>
    
    Send window is a object which uses indexes to maintain a send window. The indexes here are corresponds to the index of array like data structures and thus our send window is essentially  attached with our buffer. The implemetation enable send window to be variable size and its size should be smaller than buffer size. The class provides API to manipulate send window to get/update its state when the window is moving on buffer. Sendwindow is iterable in terms of elements inside the window.

3. **Checksum** <`ChecksumUtil.java`>

    `ChecksumUtil.java` provides API to identify corruption and convert seq, ack, payload, and sacks in a packet to byte array which will be used by a simple checksum algorithm using 1s complement addition. The checksum algorithm is unable to detect reordering bytes.

4. **Sender** <`Sender.java`>
    
    Sender is a host entity class in controls of buffering, sending, validating packets received, and doing subsequent optimized retransmissions in GBN-SACK protocol. It provides APIs for user to have a correct behavior of send window and encapsulates the validation of received sequence number and the resizing process for buffer and correlative data structures. Sender maintains objects of a send buffer, a send window, a bitMap, and a seqnum allocated for the latest packet to send. The bitMap instance in sender class is essential in GBN-SACK, being used to mark in send window the packet signaled by selective acks to reduce transmission. Both send window and bitMap are attached on send buffer and will be updated with send buffer together when resizing happens. The sender class also has a subclass named `RTTtimer` which is used to collect effective RTT, communication time, and total transmission time.


5. **Receiver** <`Receiver.java`>

    In GBN-SACK protocol, the receiver also maintains a small buffer for buffering unordered packets. Whenever it receives a expecting sequence number or not, the receiver not only acks back cumulative acks but also the sequence number of unordered packets stored in its small buffer. It aims to signal sender that it has received a portion of packets sent so the sender does not necessary to retransmit all packets in its send window.

    