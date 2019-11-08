import java.util.*;
import java.util.Random;
// import java.lang.*;
// import java.io.*;

class ChecksumHandler{
  private ChecksumHandler() {
  }
  public static int makeChecksum(int Seq, int Ack, String payload){
    int checksum = 0;
    for(int i = 0; i < payload.length();i = i+1) {
      // TODO: need to enforce aSeqnum and acknum less than 2^16 or 2^15 for signed
      checksum = checksum + (int) payload.charAt(i);
    }
    // System.out.println("checksum =                " + Integer.toBinaryString(checksum));
    // here assuming Seq and Ack are always positive and restrict to lower 16 bits.
    checksum = checksum + ((Seq>>8)+(Seq&0b0000000011111111)+(Ack>>8)+(Ack&0b0000000011111111));
    // System.out.println("Seq =                     " + Integer.toBinaryString(Seq));
    // System.out.println("Seq>>8 =                  " + Integer.toBinaryString(Seq>>8));
    // System.out.println("Seq&0b0000000011111111 =  " + Integer.toBinaryString(Seq&0b0000000011111111));
    // System.out.println("Ack =                     " + Integer.toBinaryString(Ack));
    // System.out.println("Ack>>8 =                  " + Integer.toBinaryString(Ack>>8));
    // System.out.println("Ack&0b0000000011111111 =  " + Integer.toBinaryString(Ack&0b0000000011111111));
    // System.out.println("checksum =                " + Integer.toBinaryString(checksum));
    // System.out.println("Integer.toBinaryString(checksum).length() = " + Integer.toBinaryString(checksum).length());
    while(Integer.toBinaryString(checksum).length()>16) {
      checksum = checksum - 2^(Integer.toBinaryString(checksum).length() + 1);
    }
    // System.out.println("checksum =                " + Integer.toBinaryString(checksum));
    // System.out.println("~checksum =               " + Integer.toBinaryString(~checksum));
    return ~checksum;
  }

  public static boolean checkChecksum(Packet p){
    int calculatedChecksum = ~makeChecksum(p.getSeqnum(), p.getAcknum(), p.getPayload());
    int receivedChecksum = p.getChecksum();
    // System.out.println("calculatedChecksum =                        " + Integer.toBinaryString(calculatedChecksum));
    // System.out.println("receivedChecksum   =                        " + Integer.toBinaryString(receivedChecksum));
    // System.out.println("calculatedChecksum + receivedChecksum =     " + (calculatedChecksum + receivedChecksum));
    // System.out.println("calculatedChecksum + receivedChecksum =     " + Integer.toBinaryString(calculatedChecksum + receivedChecksum));
    return calculatedChecksum + receivedChecksum == -1;
  }
  public static boolean checkChecksumE1(Packet p){
    Random rand = new Random(8429);
    // int rand_int = rand.nextInt(2^15);
    int rand_int = rand.nextInt(2^16);
    System.out.println("rand_int =                                  " + rand_int);
    int calculatedChecksum = (~(makeChecksum(p.getSeqnum(), p.getAcknum(), p.getPayload()))+rand_int)%2^16;
    int receivedChecksum = p.getChecksum();
    System.out.println("calculatedChecksum =                        " + Integer.toBinaryString(calculatedChecksum));
    System.out.println("receivedChecksum   =                        " + Integer.toBinaryString(receivedChecksum));
    System.out.println("calculatedChecksum + receivedChecksum =     " + (calculatedChecksum + receivedChecksum));
    System.out.println("calculatedChecksum + receivedChecksum =     " + Integer.toBinaryString(calculatedChecksum + receivedChecksum));
    return calculatedChecksum + receivedChecksum == -1;
  }
}
// public class operators {
//     public static void main(String[] args)
//     {
//       bWinBuffer = new PriorityQueue<Packet>(20, new PacketSeqComparator());
//     }
// }
// bWinBuffer = new PriorityQueue<Packet>(winsize, new PacketSeqComparator());
