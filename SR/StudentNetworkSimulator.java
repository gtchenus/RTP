import java.util.*;
import java.io.*;

class BufferOutOfRangeException extends Exception {
  public BufferOutOfRangeException(String err){
    super(err);
  }
}

// class PacketSeqComparator implements Comparator<Packet>{
//   public int compare(Packet p1, Packet p2) {
//     if (p1.getSeqnum() < p2.getSeqnum())
//       return -1;
//     else if (p1.getSeqnum() > p2.getSeqnum())
//       return 1;
//     return 0;
//     }
// }


public class StudentNetworkSimulator extends NetworkSimulator
{
    /*
     * Predefined Constants (static member variables):
     *
     *   int MAXDATASIZE : the maximum size of the Message data and
     *                     Packet payload
     *
     *   int A           : a predefined integer that represents entity A
     *   int B           : a predefined integer that represents entity B
     *
     * Predefined Member Methods:
     *
     *  void stopTimer(int entity):
     *       Stops the timer running at "entity" [A or B]
     *  void startTimer(int entity, double increment):
     *       Starts a timer running at "entity" [A or B], which will expire in
     *       "increment" time units, causing the interrupt handler to be
     *       called.  You should only call this with A.
     *  void toLayer3(int callingEntity, Packet p)
     *       Puts the packet "p" into the network from "callingEntity" [A or B]
     *  void toLayer5(String dataSent)
     *       Passes "dataSent" up to layer 5
     *  double getTime()
     *       Returns the current time in the simulator.  Might be useful for
     *       debugging.
     *  int getTraceLevel()
     *       Returns TraceLevel
     *  void printEventList()
     *       Prints the current event list to stdout.  Might be useful for
     *       debugging, but probably not.
     *
     *
     *  Predefined Classes:
     *
     *  Message: Used to encapsulate a message coming from layer 5
     *    Constructor:
     *      Message(String inputData):
     *          creates a new Message containing "inputData"
     *    Methods:
     *      boolean setData(String inputData):
     *          sets an existing Message's data to "inputData"
     *          returns true on success, false otherwise
     *      String getData():
     *          returns the data contained in the message
     *  Packet: Used to encapsulate a packet
     *    Constructors:
     *      Packet (Packet p):
     *          creates a new Packet that is a copy of "p"
     *      Packet (int seq, int ack, int check, String newPayload)
     *          creates a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and a
     *          payload of "newPayload"
     *      Packet (int seq, int ack, int check)
     *          chreate a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and
     *          an empty payload
     *    Methods:
     *      boolean setSeqnum(int n)
     *          sets the Packet's sequence field to "n"
     *          returns true on success, false otherwise
     *      boolean setAcknum(int n)
     *          sets the Packet's ack field to "n"
     *          returns true on success, false otherwise
     *      boolean setChecksum(int n)
     *          sets the Packet's checksum to "n"
     *          returns true on success, false otherwise
     *      boolean setPayload(String newPayload)
     *          sets the Packet's payload to "newPayload"
     *          returns true on success, false otherwise
     *      int getSeqnum()
     *          returns the contents of the Packet's sequence field
     *      int getAcknum()
     *          returns the contents of the Packet's ack field
     *      int getChecksum()
     *          returns the checksum of the Packet
     *      int getPayload()
     *          returns the Packet's payload
     *
     */

    /*   Please use the following variables in your routines.
     *   int WindowSize  : the window size
     *   double RxmtInterval   : the retransmission timeout
     *   int LimitSeqNo  : when sequence number reaches this value, it wraps around
     */

    public static final int FirstSeqNo = 0;
    private int WindowSize;
    private double RxmtInterval;
    private int LimitSeqNo;

    private double FromANum = 0;
    private double RetransNum = 0;
    private double ToLayer5Num = 0;
    private double ACKFromBNum = 0;
    private double CorruptedNum = 0;
    private double RatioLost = 0;
    private double RatioCorrupted = 0;
    // private double ARTT = 0;
    // private double ARTTCount = 0;
    private double SRTT = 0;
    private double SRTTCount = 0;
    // private double ACommTime = 0;
    private double SCommTime = 0;
    private double SCommTimeCount = 0;


    private int aSeqnum;
    private int aAckednum;
    private int bSeqnum;
    private int bAcknum;
    // private double ACommTime;
    private boolean theFirst;
    private double theFirstTime;
    private double theLastTime;
    private int MAXAWindowContentSize;
    private LinkedList<Packet> aBuffer;
    private LinkedList<Double> SendTimeListFull;
    private ArrayList<Double> SendTimeList;
    // private ArrayList<Double> RecvTimeList;
    private ArrayList<Double> SendTimeListNoRetrans;
    // private ArrayList<Double> RecvTimeListNoRetrans;
    private LinkedList<Packet> aWinBuffer;
    private PriorityQueue<Packet> bWinBuffer1;
    private PriorityQueue<Packet> bWinBuffer2;
    // private boolean bWinBuffer2isLater;

    // private ChecksumHandler ChecksumOP = new ChecksumHandler();





    // Add any necessary class variables here.  Remember, you cannot use
    // these variables to send messages error free!  They can only hold
    // state information for A or B.
    // Also add any necessary methods (e.g. checksum of a String)

    // This is the constructor.  Don't touch!
    public StudentNetworkSimulator(int numMessages,
                                   double loss,
                                   double corrupt,
                                   double avgDelay,
                                   int trace,
                                   int seed,
                                   int winsize,
                                   double delay)
    {
        super(numMessages, loss, corrupt, avgDelay, trace, seed);
      	WindowSize = winsize;
      	LimitSeqNo = winsize*2; // set appropriately; assumes SR here!
      	RxmtInterval = delay;
    }


    // This routine will be called whenever the upper layer at the sender [A]
    // has a message to send.  It is the job of your protocol to insure that
    // the data in such a message is delivered in-order, and correctly, to
    // the receiving upper layer.
    protected void aOutput(Message message)
    {
      // int nextSeq = (aSeqnum + message.getData().length())%LimitSeqNo;
      int nextSeq = (aSeqnum++)%LimitSeqNo;
      // System.out.println("aSeqnum: " + nextSeq);
      Packet p = new Packet(nextSeq, aAckednum, ChecksumHandler.makeChecksum(nextSeq, aAckednum, message.getData()), message.getData());

      if(aBuffer.size() >= 50) {
        System.out.println("aBuffer out of range!!!!");
        return;
      }
      aBuffer.add(p);
      // SendTimeListFull.add(getTime());

      if(aWinBuffer.size() >= WindowSize) {
        System.out.println("aWinBuffer out of range!!!!");
        return;
      } else {
        while(aWinBuffer.size() < WindowSize && aBuffer.size()>0){
          Packet t = aBuffer.poll();
          aWinBuffer.add(t);
          if(aWinBuffer.size()>MAXAWindowContentSize) {
            MAXAWindowContentSize = aWinBuffer.size();
          }
          toLayer3(A,t);

          FromANum = FromANum + 1;  // number of packet sent out
          if(theFirst){ // the start of duration; only set to be first packet sent out
            theFirstTime=getTime();
            theFirst=false;
          }

          // double td = SendTimeListFull.poll();
          SendTimeList.set(t.getSeqnum(), getTime());
          SendTimeListNoRetrans.set(t.getSeqnum(), getTime());

          System.out.println("in aOutput(): "+t.toString());
          stopTimer(A);
          startTimer(A, RxmtInterval);
        }
      }
      // stopTimer(A);
      // startTimer(A, RxmtInterval);
    }

    // This routine will be called whenever a packet sent from the B-side
    // (i.e. as a result of a toLayer3() being done by a B-side procedure)
    // arrives at the A-side.  "packet" is the (possibly corrupted) packet
    // sent from the B-side.
    protected void aInput(Packet packet)
    {
      // receiving ACK
      // check if packet corrupted
      System.out.println("received at aInput()1: "+packet.toString());
      if(ChecksumHandler.checkChecksum(packet)) {
        System.out.println("received at aInput()2: "+packet.toString());
        // I don't have to do (packet.getAcknum()<= aAckednum||packet.getAcknum()> aAckednum+WindowSize), as I only send pkts with seq in windowsize.
        // dup without wrapping
        if((aAckednum+WindowSize)<LimitSeqNo&&packet.getAcknum()<= aAckednum){ // dup ack: the AKCed number has arrived previously.
          if(aWinBuffer.peek()!=null) {
            Packet t = aWinBuffer.peek();
            toLayer3(A,t); // retrans next unACK'ed

            RetransNum = RetransNum + 1;
            // SendTimeListNoRetrans.set(t.getSeqnum(), -1.0);
            for (Packet p: aWinBuffer) { // removing all subsequent ACK which are safely received at receiver and send back because of recent retrans
              SendTimeListNoRetrans.set(p.getSeqnum(), -1.0);
            }

            System.out.println("in aInput() dup1: "+ aWinBuffer.peek().toString());
            stopTimer(A);
            startTimer(A, RxmtInterval);
            return;
          }
          stopTimer(A);
          return;
        }
        // dup with wrapping
        if((aAckednum+WindowSize)>=LimitSeqNo&&(packet.getAcknum()<= aAckednum&&packet.getAcknum()>(aAckednum+WindowSize-LimitSeqNo))){ // dup ack: the AKCed number has arrived previously.
          if(aWinBuffer.peek()!=null) {
            Packet t = aWinBuffer.peek();
            toLayer3(A,t); // retrans next unACK'ed

            RetransNum = RetransNum + 1;
            // SendTimeListNoRetrans.set(t.getSeqnum(), -1.0);
            for (Packet p: aWinBuffer) { // removing all subsequent ACK which are safely received at receiver and send back because of recent retrans
              SendTimeListNoRetrans.set(p.getSeqnum(), -1.0);
            }

            System.out.println("in aInput() dup2: "+ aWinBuffer.peek().toString());
            stopTimer(A);
            startTimer(A, RxmtInterval);
            return;
          }
          stopTimer(A);
          return;
        }

        // moving window forward
        // this is safe for cumulative ack
        Iterator<Packet> it = aWinBuffer.iterator();
        while (it.hasNext()) {
            Packet p = it.next();
            if(p.getSeqnum() != packet.getAcknum()) { // remove all with "less" seq num
                aAckednum = p.getSeqnum();
                it.remove();

                SCommTime = SCommTime + (getTime()-SendTimeList.get(p.getSeqnum()));
                SCommTimeCount++;

                System.out.println("dumpping "+ p.toString());
            } else { // until I find the element with same seq num
              aAckednum = p.getSeqnum();
              it.remove();

              SCommTime = SCommTime + (getTime()-SendTimeList.get(p.getSeqnum()));
              SCommTimeCount++;      // each is a successful delivery
              theLastTime=getTime(); // duration end; keep update to current
              if(SendTimeListNoRetrans.get(p.getSeqnum())!=-1){ // -1 means null
                SRTT = SRTT + getTime()-SendTimeListNoRetrans.get(p.getSeqnum());
                SRTTCount = SRTTCount+1;
                SendTimeListNoRetrans.set(p.getSeqnum(), -1.0);
              }

              System.out.println("dumpping "+ p.toString());
              break;
            }
        }
        // if no new and no waiting
        if(aBuffer.size()<=0&&aWinBuffer.size()<=0) {
          stopTimer(A);
          return;
        }
        // if no new to trans
        if(aBuffer.size()<=0) {
          stopTimer(A);
          startTimer(A, RxmtInterval);
          return;
        }

        // trans new elements
        while(aWinBuffer.size()<WindowSize && aBuffer.size()>0) {
          Packet p = aBuffer.poll();
          aWinBuffer.add(p);
          toLayer3(A, p);
          FromANum = FromANum + 1;  // number of packet sent out
          // double td = SendTimeListFull.poll();
          SendTimeList.set(p.getSeqnum(), getTime());
          SendTimeListNoRetrans.set(p.getSeqnum(), getTime());
          System.out.println("in aInput() trans new: "+ p.toString());
        }
        stopTimer(A);
        startTimer(A, RxmtInterval);

      } else {
        System.out.println("in aInput(), pkt recv'ed corrupted: "+ packet.toString());
        CorruptedNum = CorruptedNum + 1;
      }

    }

    // This routine will be called when A's timer expires (thus generating a
    // timer interrupt). You'll probably want to use this routine to control
    // the retransmission of packets. See startTimer() and stopTimer(), above,
    // for how the timer is started and stopped.
    protected void aTimerInterrupt()
    {
      // this is a timer for all entries; if timeout, I may transmit the first element to best approximate SR i.e. insert missing elements. Or the receiver buffer do not make sense.
      Packet t = aWinBuffer.peek();
      toLayer3(A,t);
      RetransNum = RetransNum + 1;
      for (Packet p: aWinBuffer) { // removing all subsequent ACK which are safely received at receiver and send back because of recent retrans.
        SendTimeListNoRetrans.set(p.getSeqnum(), -1.0);
      }

      System.out.println("in aTimerInterrupt(): "+ aWinBuffer.peek().toString());
      startTimer(A, RxmtInterval);
    }

    // This routine will be called once, before any of your other A-side
    // routines are called. It can be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of entity A).
    protected void aInit()
    {
      MAXAWindowContentSize = 0;
      aSeqnum = FirstSeqNo;
      aAckednum = FirstSeqNo-1; // -1 means uninitialized
      aBuffer = new LinkedList<Packet>();
      aWinBuffer = new LinkedList<Packet>();
      SendTimeListFull = new LinkedList<Double>();
      SendTimeList = new ArrayList<Double>(Collections.nCopies(LimitSeqNo, -1.0));
      SendTimeListNoRetrans = new ArrayList<Double>(Collections.nCopies(LimitSeqNo, -1.0));
      theFirst = true;
      theFirstTime = 0.0;
      theLastTime = 0.0;
    }

    // This routine will be called whenever a packet sent from the B-side
    // (i.e. as a result of a toLayer3() being done by an A-side procedure)
    // arrives at the B-side.  "packet" is the (possibly corrupted) packet
    // sent from the A-side.
    protected void bInput(Packet packet)
    {
      System.out.println("in bInput(): "+ packet.toString());
      if(ChecksumHandler.checkChecksum(packet)) {
        // without wrapping; check received seq out of window size (check dup)
        if((bAcknum+WindowSize)<LimitSeqNo&&(packet.getSeqnum()<=(bAcknum)||(packet.getSeqnum())>((bAcknum)+WindowSize))){
          System.out.println("in bInput(): dup without wrapping");
          Packet e = new Packet(bSeqnum, bAcknum, ChecksumHandler.makeChecksum(bSeqnum, bAcknum, ""), "");
          toLayer3(B, e);
          ACKFromBNum = ACKFromBNum + 1;
          return;
        }
        // with wrapping; check received seq out of window size (check dup)
        if((bAcknum+WindowSize)>=LimitSeqNo&&(packet.getSeqnum()<=(bAcknum)&&(packet.getSeqnum())>((bAcknum)+WindowSize-LimitSeqNo))){
          System.out.println("in bInput(): dup with wrapping");
          Packet e = new Packet(bSeqnum, bAcknum, ChecksumHandler.makeChecksum(bSeqnum, bAcknum, ""), "");
          toLayer3(B, e);
          ACKFromBNum = ACKFromBNum + 1;
          return;
        }

        System.out.println("received at bInput(): "+packet.toString());
        System.out.println("bAcknum: "+bAcknum);
        System.out.println("LimitSeqNo: "+LimitSeqNo);
        System.out.println("bWinBuffer1.size()1: "+bWinBuffer1.size());

        bWinBuffer1 = new PriorityQueue<Packet>(bWinBuffer1);
        bWinBuffer2 = new PriorityQueue<Packet>(bWinBuffer2);
        // bWinBuffer2 always hold elements later than that in bWinBuffer1
        // handling wrapping issue.
        if(packet.getSeqnum()<(bAcknum+1)%LimitSeqNo) { // with wrapping
          bWinBuffer2.add(packet);
        } else {                                        // without wrapping
          bWinBuffer1.add(packet);
        }

        System.out.println("bWinBuffer1.size()2: "+bWinBuffer1.size());
        // missing first element in the rec window (out of order element received). cannot move window forward
        if((bWinBuffer1.peek()==null||((bAcknum+1)%LimitSeqNo)!=(bWinBuffer1.peek().getSeqnum()))){
          System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<< go backward!");
          if(bWinBuffer1.peek()!=null) {
            System.out.println("bWinBuffer1.peek().toString(): "+bWinBuffer1.peek().toString());
          }
          Packet e = new Packet(bSeqnum, bAcknum, ChecksumHandler.makeChecksum(bSeqnum, bAcknum, ""), "");
          toLayer3(B, e);
          ACKFromBNum = ACKFromBNum + 1;
          return;
        }

        // looping to check how far I can move window forward
        Packet t = null;
        while((bWinBuffer1.peek()!=null)&&(((bAcknum+1)%LimitSeqNo)==(bWinBuffer1.peek().getSeqnum()))){
          System.out.println("bWinBuffer1.peek().toString(): "+bWinBuffer1.peek().toString());
          t = bWinBuffer1.poll();
          System.out.println("bWinBuffer1.size()3: "+bWinBuffer1.size());
          bAcknum = (bAcknum+1)%LimitSeqNo;
          toLayer5(t.getPayload());
          ToLayer5Num = ToLayer5Num + 1;
          while((bWinBuffer1.peek()!=null)&&(((bAcknum)%LimitSeqNo)==(bWinBuffer1.peek().getSeqnum()))){
            bWinBuffer1.poll();
          }
        }
        // also check for bWinBuffer2, in case of wrapping
        if(bWinBuffer1.size()<=0) {
          Packet p = null;
          while((bWinBuffer2.peek()!=null)&&(((bAcknum+1)%LimitSeqNo)==(bWinBuffer2.peek().getSeqnum()))){
            System.out.println("bWinBuffer2.peek().toString(): "+bWinBuffer2.peek().toString());
            p = bWinBuffer2.poll();
            System.out.println("bWinBuffer2.size()3: "+bWinBuffer2.size());
            bAcknum = (bAcknum+1)%LimitSeqNo;
            toLayer5(p.getPayload());
            ToLayer5Num = ToLayer5Num + 1;
            while((bWinBuffer1.peek()!=null)&&(((bAcknum)%LimitSeqNo)==(bWinBuffer1.peek().getSeqnum()))){
              bWinBuffer1.poll();
            }

            // bWinBuffer1 = new PriorityQueue<Packet>(bWinBuffer2);
            // bWinBuffer2 = new PriorityQueue<Packet>(WindowSize, new PacketSeqComparator());
          }
          if(p!=null) {
            bWinBuffer1 = new PriorityQueue<Packet>(bWinBuffer2);
            bWinBuffer2 = new PriorityQueue<Packet>(WindowSize, new PacketSeqComparator());
            t=p;
          }
        }

        // we did move window forward
        if(t!=null) {
          bSeqnum = (bSeqnum+1)%LimitSeqNo;
          Packet e = new Packet(bSeqnum, t.getSeqnum(), ChecksumHandler.makeChecksum(bSeqnum, t.getSeqnum(), ""), "");
          toLayer3(B, e);
          ACKFromBNum = ACKFromBNum + 1;
          System.out.println("from bInput(): "+e.toString());
        }

        // System.out.println("bWinBuffer1.peek().toString()2: "+bWinBuffer1.peek().toString());
      } else {
        System.out.println("in bInput(), pkt recv'ed corrupted: "+ packet.toString());
        CorruptedNum = CorruptedNum + 1;
        Packet e = new Packet(bSeqnum, bAcknum, ChecksumHandler.makeChecksum(bSeqnum, bAcknum, ""), "");
        toLayer3(B, e);
      }
    }

    // This routine will be called once, before any of your other B-side
    // routines are called. It can be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of entity B).
    protected void bInit()
    {
      bSeqnum = FirstSeqNo-1;
      bAcknum = FirstSeqNo-1;
      bWinBuffer1 = new PriorityQueue<Packet>(WindowSize, new PacketSeqComparator());
      bWinBuffer2 = new PriorityQueue<Packet>(WindowSize, new PacketSeqComparator());
    }

    // Use to print final statistics
    protected void Simulation_done()
    {
    	// TO PRINT THE STATISTICS, FILL IN THE DETAILS BY PUTTING VARIBALE NAMES. DO NOT CHANGE THE FORMAT OF PRINTED OUTPUT
    	System.out.println("\n\n===============STATISTICS=======================");
    	System.out.println("Number of original packets transmitted by A: " + FromANum);
    	System.out.println("Number of retransmissions by A:" + RetransNum);
    	System.out.println("Number of data packets delivered to layer 5 at B:" + ToLayer5Num);
    	System.out.println("Number of ACK packets sent by B:" + ACKFromBNum);
    	System.out.println("Number of corrupted packets:" + CorruptedNum);
    	System.out.println("Ratio of lost packets:" + (RetransNum - CorruptedNum) / ((FromANum + RetransNum) + ACKFromBNum) );
    	System.out.println("Ratio of corrupted packets:" + (CorruptedNum) / ((FromANum + RetransNum) + ACKFromBNum - (RetransNum - CorruptedNum)));
    	System.out.println("Average RTT:" + SRTT/SRTTCount);
    	System.out.println("Average communication time:" + SCommTime/SCommTimeCount);
    	System.out.println("==================================================");

    	// PRINT YOUR OWN STATISTIC HERE TO CHECK THE CORRECTNESS OF YOUR PROGRAM
    	System.out.println("\nEXTRA:");
    	// EXAMPLE GIVEN BELOW
    	//System.out.println("Example statistic you want to check e.g. number of ACK packets received by A :" + "<YourVariableHere>");
      System.out.println("MAXAWindowContentSize:" + MAXAWindowContentSize);
      System.out.println("SRTTCount:" + SRTTCount);
      System.out.println("SRTT:" + SRTT);
      System.out.println("SCommTimeCount:" + SCommTimeCount);
      System.out.println("SCommTime:" + SCommTime);
      System.out.println("Duration:" + (theLastTime-theFirstTime));
      System.out.println("Throughput:" + (RetransNum+FromANum)/(theLastTime-theFirstTime));
      System.out.println("Goodput:   " + (SCommTimeCount)/(theLastTime-theFirstTime));
    }

}
