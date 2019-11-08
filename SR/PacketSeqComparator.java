import java.util.*;
// import java.lang.*;
// import java.io.*;

class PacketSeqComparator implements Comparator<Packet>{
  public int compare(Packet p1, Packet p2) {
    if (p1.getSeqnum() < p2.getSeqnum())
      return -1;
    else if (p1.getSeqnum() > p2.getSeqnum())
      return 1;
    return 0;
    }
}
// public class operators {
//     public static void main(String[] args)
//     {
//       bWinBuffer = new PriorityQueue<Packet>(20, new PacketSeqComparator());
//     }
// }
// bWinBuffer = new PriorityQueue<Packet>(winsize, new PacketSeqComparator());
