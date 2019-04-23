package com.example.m2bikson.myrubik2018;

import java.util.Random;

public class RubikCube {

     // RLFBUD is the face order used for input, so that a correctly oriented
    // piece in the input has its 'highest value' facelet first. The rest of the
    // program uses moves in FBRLUD order.
    private static String faces = "RLFBUD";

    // I use char arrays here cause they can be initialised with a string
    // which is shorter than initialising other arrays.
    // Internally cube uses slightly different ordering to the input so that
    // orbits of stage 4 are contiguous. Note also that the two corner orbits
    // are diametrically opposite each other.
    // input: UF UR UB UL DF DR DB DL FR FL BR BL UFR URB UBL ULF DRF DFL DLB
    // DBR
    // A B C D E F G H I J K L M N O P Q R S T
    // A E C G B F D H I J K L M S N T R O Q P
    // intrnl: UF DF UB DB UR DR UL DL FR FL BR BL UFR UBL DFL DBR DLB DRF URB
    // ULF
    private static char[] order = "AECGBFDHIJKLMSNTROQP".toCharArray();

    // To quickly recognise the pieces, I construct an integer by setting a bit
    // for each
    // facelet. The unique result is then found on the list below to map it to
    // the correct
    // cubelet of the cube.
    // intrnl: UF DF UB DB UR DR UL DL FR FL BR BL UFR UBL DFL DBR DLB DRF URB
    // ULF
    // bithash:20,36,24,40, 17,33,18,34, 5, 6, 9, 10, 21, 26, 38, 41, 42, 37,
    // 25, 22
    private static char[] bithash = "TdXhQaRbEFIJUZfijeYV".toCharArray();

    // Each move consists of two 4-cycles. This string contains these in FBRLUD
    // order.
    // intrnl: UF DF UB DB UR DR UL DL FR FL BR BL UFR UBL DFL DBR DLB DRF URB
    // ULF
    // A B C D E F G H I J K L M N O P Q R S T
    private static char[] perm = "AIBJTMROCLDKSNQPEKFIMSPRGJHLNTOQAGCEMTNSBFDHORPQ".toCharArray();
    // current cube position
    private static char[] pos = new char[20]; //change to int will work well also!
    private static char[] ori = new char[20];//change to int will work well also!
    private static char[] val = new char[20];//change to int will work well also!
    // pruning tables, 2 for each phase
    private static char[][] tables = new char[8][];
    // current phase solution
    private static int[] move = new int[20];
    private static int[] moveamount = new int[20];
    // current phase being searched (0,2,4,6 for phases 1 to 4)
    private static int phase = 0;
    // Length of pruning tables. (one dummy in phase 1);
    private static int[] tablesize = {1, 4096, 6561, 4096, 256, 1536, 13824, 576};

    // number 65='A' is often subtracted to convert char ABC... to number 0,1,2,..
    private static int CHAROFFSET = 65;
    private static char[] tmpStr = new char[20];


    static char[][] RubiksColor = new char[6][9];
    static char[][] RubiksColorUpdate = new char[6][9];

    static int mainU = 4;//the value is matched to the String faces = "RLFBUD"
    static int mainD = 5;
    static int mainF = 2;
    static int mainB = 3;
    static int mainR = 0;
    static int mainL = 1;

    //Manually input the rubik cube
    //				   0	1	 2	  3	   4	5	 6	  7	   8
/*	static char[] top = 	{ 'U', 'W', 'G', 'Y', 'O', 'G', 'G', 'R', 'R' };
    static char[] bottom = { 'O', 'Y', 'Y', 'O', 'B', 'W', 'R', 'W', 'O' };
	static char[] front = 	{ 'W', 'W', 'G', 'R', 'R', 'B', 'W', 'G', 'B' };
	static char[] back = 	{ 'Y', 'R', 'G', 'O', 'W', 'B', 'Y', 'G', 'G' };
	static char[] right = 	{ 'W', 'O', 'O', 'R', 'O', 'B', 'B', 'B', 'W' };
	static char[] left = 	{ 'Y', 'Y', 'R', 'Y', 'B', 'O', 'B', 'G', 'Y' };*/

    static String sInputRandom = "";

    static int U = 4;//follow the order of RLFBUD , face used for input
    static int D = 5;
    static int F = 2;
    static int B = 3;
    static int R = 0;
    static int L = 1;


    public String getRubikValueAndSolve(char[] top, char[] bottom, char[] front, char[] back, char[] right, char[] left) {
        String solutionString;
//		SolvedRubiksColor();	//init: generate a solved rubik


        {
            RubiksColor[mainU] = top;//use pointer of arrays, then top will have the value of RubiksColor
            RubiksColor[mainD] = bottom;
            RubiksColor[mainF] = front;
            RubiksColor[mainB] = back;
            RubiksColor[mainR] = right;
            RubiksColor[mainL] = left;

            String randomxyzStringRight = new String(RubiksColor[mainR]);
            String randomxyzStringLeft = new String(RubiksColor[mainL]);
            String randomxyzStringFront = new String(RubiksColor[mainF]);
            String randomxyzStringBack = new String(RubiksColor[mainB]);
            String randomxyzStringTop = new String(RubiksColor[mainU]);
            String randomxyzStringDown = new String(RubiksColor[mainD]);

            System.out.println("random Rubik" + "  Right	" + randomxyzStringRight);
            System.out.println("Left	" + randomxyzStringLeft);
            System.out.println("Front	" + randomxyzStringFront);
            System.out.println("Back	" + randomxyzStringBack);
            System.out.println("Top	" + randomxyzStringTop);
            System.out.println("Down	" + randomxyzStringDown);

            //String str = "RU LF UB DR DL BL UL FU BD RF BR FD LDF LBD FUL RFD UFR RDB UBL RBU";//sample of the input string after generated
            String str;
            str = generateRubikInputString();// generate the input string after generates a random rubik

            solutionString = GetResult(str);        //find the solution with the method of GetResult()

            String[] solutionOperation = solutionString.split(" "); //turn into an array

            System.out.println("Length of solution:  " + solutionOperation.length + solutionString);

            for (int kk = 0; kk < solutionOperation.length; kk++) {        //check the solution: follow the steps to twist the rubik cube, if the cube becomes back to a solved cube
                if (solutionOperation[kk].equals("U1")) {
                    updateOldRubiksColor();
                    U1();
                } else if (solutionOperation[kk].equals("U2")) {
                    updateOldRubiksColor();
                    U2();
                } else if (solutionOperation[kk].equals("U3")) {
                    updateOldRubiksColor();
                    U3();
                } else if (solutionOperation[kk].equals("D1")) {
                    updateOldRubiksColor();
                    D1();
                } else if (solutionOperation[kk].equals("D2")) {
                    updateOldRubiksColor();
                    D2();
                } else if (solutionOperation[kk].equals("D3")) {
                    updateOldRubiksColor();
                    D3();
                } else if (solutionOperation[kk].equals("F1")) {
                    updateOldRubiksColor();
                    F1();
                } else if (solutionOperation[kk].equals("F2")) {
                    updateOldRubiksColor();
                    F2();
                } else if (solutionOperation[kk].equals("F3")) {
                    updateOldRubiksColor();
                    F3();
                } else if (solutionOperation[kk].equals("B1")) {
                    updateOldRubiksColor();
                    B1();
                } else if (solutionOperation[kk].equals("B2")) {
                    updateOldRubiksColor();
                    B2();
                } else if (solutionOperation[kk].equals("B3")) {
                    updateOldRubiksColor();
                    B3();
                } else if (solutionOperation[kk].equals("R1")) {
                    updateOldRubiksColor();
                    R1();
                } else if (solutionOperation[kk].equals("R2")) {
                    updateOldRubiksColor();
                    R2();
                } else if (solutionOperation[kk].equals("R3")) {
                    updateOldRubiksColor();
                    R3();
                } else if (solutionOperation[kk].equals("L1")) {
                    updateOldRubiksColor();
                    L1();
                } else if (solutionOperation[kk].equals("L2")) {
                    updateOldRubiksColor();
                    L2();
                } else if (solutionOperation[kk].equals("L3")) {
                    updateOldRubiksColor();
                    L3();
                }
            }

            String solvedStringRight = new String(RubiksColor[mainR]);
            String solvedStringLeft = new String(RubiksColor[mainL]);
            String solvedStringFront = new String(RubiksColor[mainF]);
            String solvedStringBack = new String(RubiksColor[mainB]);
            String solvedStringTop = new String(RubiksColor[mainU]);
            String solvedStringDown = new String(RubiksColor[mainD]);

            System.out.println("solved  " + "  Right	" + solvedStringRight);
            System.out.println("Left	" + solvedStringLeft);
            System.out.println("Front	" + solvedStringFront);
            System.out.println("Back	" + solvedStringBack);
            System.out.println("Top	" + solvedStringTop);
            System.out.println("Down	" + solvedStringDown);

            return solutionString;
        }
    }

    public static String GetResult(String sInput) {


         phase = 0;
        //String sInput = "RU LF UB DR DL BL UL FU BD RF BR FD LDF LBD FUL RFD UFR RDB UBL RBU";
        String[] argv = sInput.split(" ");

        String sOutput = "";
        // String[] argv = {"RU", "LF", "UB", "DR", "DL", "BL", "UL", "FU", "BD", "RF", "BR", "FD", "LDF", "LBD", "FUL", "RFD", "UFR", "RDB", "UBL", "RBU"};

        if (argv.length != 20) {
            //return "error";
            return "errors";
        }

        int f, i = 0, j = 0, k = 0, pc, mor;
        // initialise tables
        for (; k < 20; k++) {
            tmpStr = Character.toChars(k < 12 ? 2 : 3); //if k<12, (2); if k>=12, (3)
            val[k] = tmpStr[0];
        }

        for (; j < 8; j++)
            filltable(j);
		
//////////////////////////////////////
        // read input, 20 pieces worth
        for (; i < 20; i++) {
            f = pc = k = mor = 0;
            for (; f < val[i]; f++) {
                // read input from stdin, or...
                //     do{cin>>c;}while(c==' ');
                //     j=strchr(faces,c)-faces;
                // ...from command line and get face number of facelet
                j = faces.indexOf(argv[i].charAt(f));
                // keep track of principal facelet for orientation
                if (j > k) {
                    k = j;
                    mor = f;
                }
                //construct bit hash code
                pc += 1 << j;
            }
            // find which cubelet it belongs, i.e. the label for this piece
            for (f = 0; f < 20; f++)
                if (pc == bithash[f] - 64)
                    break;
            // store piece
            tmpStr = Character.toChars(f);
            pos[order[i] - CHAROFFSET] = tmpStr[0];
            tmpStr = Character.toChars(mor % val[i]);
            ori[order[i] - CHAROFFSET] = tmpStr[0];
        }

        //solve the cube
        // four phases
        for (; phase < 8; phase += 2) {
            // try each depth till solved
            for (j = 0; !searchphase(j, 0, 9); j++)
                ;
            //output result of this phase
            for (i = 0; i < j; i++) {
                sOutput += "FBRLUD".charAt(move[i]) + "" + moveamount[i];
                sOutput += " ";
            }
        }


        for (; phase < 8; phase += 2) {
            for (j = 0; !searchphase(j, 0, 9); j++)
                ;
            for (i = 0; i < j; i++) {
                sOutput += "FBRLUD".charAt(move[i]) + "" + moveamount[i];
                sOutput += " ";
            }
        }


        return sOutput;

    }

    public static int Char2Num(char c) {
        return (int) c - CHAROFFSET;
    }

    // Cycles 4 pieces in array p, the piece indices given by a[0..3].
    public static void cycle(char[] p, char[] a, int offset) {
        char temp = p[Char2Num(a[0 + offset])];
        p[Char2Num(a[0 + offset])] = p[Char2Num(a[1 + offset])];
        p[Char2Num(a[1 + offset])] = temp;
        temp = p[Char2Num(a[0 + offset])];
        p[Char2Num(a[0 + offset])] = p[Char2Num(a[2 + offset])];
        p[Char2Num(a[2 + offset])] = temp;
        temp = p[Char2Num(a[0 + offset])];
        p[Char2Num(a[0 + offset])] = p[Char2Num(a[3 + offset])];
        p[Char2Num(a[3 + offset])] = temp;
    }

    // twists i-th piece a+1 times.
    public static void twist(int i, int a) {
        i -= CHAROFFSET;
        tmpStr = Character.toChars(((int) ori[i] + a + 1) % val[i]);
        ori[i] = tmpStr[0];
    }

    // set cube to solved position
    public static void reset() {
        for (int i = 0; i < 20; ) {
            tmpStr = Character.toChars(i);
            pos[i] = tmpStr[0];
            ori[i++] = '\0';
        }
    }

    // convert permutation of 4 chars to a number in range 0..23
    public static int permtonum(char[] p, int offset) {
        int n = 0;
        int temp1;
        int temp2;
        int b;
        for (int a = 0; a < 4; a++) {
            n *= 4 - a;
            for (b = a; ++b < 4; )
                if (p[b + offset] < p[a + offset])
                    n++;
        }
        return n;
    }

    // convert number in range 0..23 to permutation of 4 chars.
    public static void numtoperm(char[] p, int n, int o) {
        tmpStr = Character.toChars(o);
        p[3 + o] = tmpStr[0];
        for (int a = 3; a-- > 0; ) {
            tmpStr = Character.toChars(n % (4 - a) + o);
            p[a + o] = tmpStr[0];
            n /= 4 - a;
            for (int b = a; ++b < 4; )
                if (p[b + o] >= p[a + o])
                    p[b + o]++;
        }
    }

    // get index of cube position from table t
    public static int getposition(int t) {    //t =0: return 0;
        int i = -1, n = 0;
        switch (t) {
            // case 0 does nothing so returns 0
            case 1://edgeflip
                // 12 bits, set bit if edge is flipped
                for (; ++i < 12; )
                    n += ((int) ori[i]) << i;
                break;
            case 2: //cornertwist
                // get base 3 number of 8 digits - each digit is corner twist
                for (i = 20; --i > 11; )
                    n = n * 3 + (int) ori[i];
                break;
            case 3: //middle edge choice
                // 12 bits, set bit if edge belongs in Um middle slice
                for (; ++i < 12; )
                    n += ((((int) pos[i]) & 8) > 0) ? (1 << i) : 0;
                break;
            case 4: //ud slice choice
                // 8 bits, set bit if UD edge belongs in Fm middle slice
                for (; ++i < 8; )
                    n += ((((int) pos[i]) & 4) > 0) ? (1 << i) : 0;
                break;
            case 5: //tetrad choice, twist and parity
                int[] corn = new int[8];
                int[] corn2 = new int[4];
                int j,
                        k,
                        l;
                // 8 bits, set bit if corner belongs in second tetrad.
                // also separate pieces for twist/parity determination
                k = j = 0;
                for (; ++i < 8; )
                    if (((l = pos[i + 12] - 12) & 4) > 0) {
                        corn[l] = k++;
                        n += 1 << i;
                    } else
                        corn[j++] = l;
                //Find permutation of second tetrad after solving first
                for (i = 0; i < 4; i++)
                    corn2[i] = corn[4 + corn[i]];
                //Solve one piece of second tetrad
                for (; --i > 0; )
                    corn2[i] ^= corn2[0];
                // encode parity/tetrad twist
                n = n * 6 + corn2[1] * 2 - 2;
                if (corn2[3] < corn2[2])
                    n++;
                break;
            case 6://two edge and one corner orbit, permutation
                n = permtonum(pos, 0) * 576 + permtonum(pos, 4) * 24
                        + permtonum(pos, 12);
                break;
            case 7: //one edge and one corner orbit, permutation
                n = permtonum(pos, 8) * 24 + permtonum(pos, 16);
                break;

        }
        return n;
    }

    // sets cube to any position which has index n in table t
    public static void setposition(int t, int n) {

        int i = 0, j = 12, k = 0;
        char[] corn = "QRSTQRTSQSRTQTRSQSTRQTSR".toCharArray();
        reset();
        switch (t) {
            // case 0 does nothing so leaves cube solved
            case 1:    //edgeflip
                for (; i < 12; i++, n >>= 1) {
                    tmpStr = Character.toChars(n & 1);
                    ori[i] = tmpStr[0];
                }
                break;
            case 2:    //cornertwist
                for (i = 12; i < 20; i++, n /= 3) {
                    tmpStr = Character.toChars(n % 3);
                    ori[i] = tmpStr[0];
                }
                break;
            case 3: //middle edge choice
                for (; i < 12; i++, n >>= 1) {
                    tmpStr = Character.toChars(8 * n & 8);
                    pos[i] = tmpStr[0];
                }
                break;
            case 4://ud slice choice
                for (; i < 8; i++, n >>= 1) {
                    tmpStr = Character.toChars(4 * n & 4);
                    pos[i] = tmpStr[0];
                }
                break;
            case 5://tetrad choice,parity,twist
                int offset = n % 6 * 4;
                n /= 6;
                for (; i < 8; i++, n >>= 1) {
                    tmpStr = Character.toChars(((n & 1) > 0) ? corn[offset + k++]
                            - CHAROFFSET : j++);
                    pos[i + 12] = tmpStr[0];
                }
                break;
            case 6://slice permutations
                numtoperm(pos, n % 24, 12);
                n /= 24;
                numtoperm(pos, n % 24, 4);
                n /= 24;
                numtoperm(pos, n, 0);
                break;
            case 7://corner permutations
                numtoperm(pos, n / 24, 8);
                numtoperm(pos, n % 24, 16);
                break;
        }
    }

    //do a clockwise quarter turn cube move
    public static void domove(int m) {

        int offset = 8 * m;
        int i = 8;

        //cycle the edges
        cycle(pos, perm, offset);

        //solvedStringPos2 = pos.toString();
        //solvedStringOri2 = ori.toString();

        cycle(ori, perm, offset);

        //solvedStringPos3 = pos.toString();
        //solvedStringOri3 = ori.toString();
        //cycle the corners
        cycle(pos, perm, offset + 4);

        //solvedStringPos4 = pos.toString();
        //solvedStringOri4 = ori.toString();

        cycle(ori, perm, offset + 4);

        //solvedStringPos5 = pos.toString();
        //solvedStringOri5 = ori.toString();

        //twist corners if RLFB
        if (m < 4)
            for (; --i > 3; )
                twist(perm[i + offset], i & 1);

        //solvedStringPos6 = pos.toString();
        //solvedStringOri6 = ori.toString();

        //flip edges if FB
        if (m < 2)
            for (i = 4; i-- > 0; )
                twist(perm[i + offset], 0);
		
    }

    // calculate a pruning table
    public static void filltable(int ti) {
        int n = 1, l = 1, tl = tablesize[ti];
        // alocate table memory
        char[] tb = new char[tl];
        tables[ti] = tb;    //static char[][] tables = new char[8][];  tables is a 2D array. tables[ti] represents a single row in the 2D array: tables[ti][0] to tables[ti][j=length]
        for (int i = 0; i < tb.length; i++)
            tb[i] = '\0';
        //mark solved position as depth 1
        reset();
        tmpStr = Character.toChars(1);
        tb[getposition(ti)] = tmpStr[0];
        // while there are positions of depth l
        while (n > 0) {
            n = 0;
            // find each position of depth l
            for (int i = 0; i < tl; i++) {
                if (tb[i] == l) {
                    //construct that cube position
                    setposition(ti, i);
                    // try each face any amount
                    for (int f = 0; f < 6; f++) {
                        for (int q = 1; q < 4; q++) {
                            domove(f);
                            // get resulting position
                            int r = getposition(ti);
                            // if move as allowed in that phase, and position is a new one
                            if ((q == 2 || f >= (ti & 6)) && tb[r] == '\0') { //Binary AND Operator  6= 00000110 , ti =0:00000000
                                tmpStr = Character.toChars(l + 1);
                                tb[r] = tmpStr[0]; // mark that position as depth l+1
                                n++;
                            }
                        }
                        domove(f);
                    }
                }
            }
            l++;
        }
    }

    // Pruned tree search. recursive.
    public static boolean searchphase(int movesleft, int movesdone, int lastmove) {
        // prune - position must still be solvable in the remaining moves available
        if (tables[phase][getposition(phase)] - 1 > movesleft
                || tables[phase + 1][getposition(phase + 1)] - 1 > movesleft)
            return false;
        // If no moves left to do, we have solved this phase
        if (movesleft == 0)
            return true;

        // not solved. try each face move
        for (int i = 6; i-- > 0; ) {
            // do not repeat same face, nor do opposite after DLB.
            if ((i - lastmove != 0)
                    && ((i - lastmove + 1) != 0 || ((i | 1) != 0))) {
                move[movesdone] = i;
                // try 1,2,3 quarter turns of that face
                for (int j = 0; ++j < 4; ) {
                    //do move and remember it
                    domove(i);
                    moveamount[movesdone] = j;
                    //Check if phase only allows half moves of this face
                    if ((j == 2 || i >= phase)
                            //search on
                            && searchphase(movesleft - 1, movesdone + 1, i))
                        return true;
                }
                // put face back to original position.
                domove(i);
            }
        }
        // no solution found
        return false;
    }

    static void SolvedRubiksColor() {
        int i, j;

        for (i = 0; i < 6; i++) {
            for (j = 0; j < 9; j++) {
                switch (i) {
                    case 0:
                        RubiksColor[i][j] = 'R';//RIGHT, and following the order of String faces = "RLFBUD";
                        break;
                    case 1:
                        RubiksColor[i][j] = 'L';
                        break;
                    case 2:
                        RubiksColor[i][j] = 'F';
                        break;
                    case 3:
                        RubiksColor[i][j] = 'B';
                        break;
                    case 4:
                        RubiksColor[i][j] = 'U';
                        break;
                    case 5:
                        RubiksColor[i][j] = 'D';
                        break;
                    default:
                        break;
                }

            }
        }
    }

    // BUT  RLFBUD is the face order used for input
/**    int cycles[][] = { { 0, 1, 2, 3 }, // R
 { 4, 5, 6, 7 }, // L
 { 3, 11, 7, 10 }, // F
 { 1, 8, 5, 9 }, // B
 { 0, 8, 4, 11 }, // U
 { 2, 9, 6, 10 } // D
 };

 |****U=4*****|
 |*U0**U1**U2*|
 |************|
 |*U3**U4**U5*|
 |************|
 |*U6**U7**U8*|
 |************|
 |****L=0******|****F=2*****|****R=0*****|****B=3*****|
 |*L0**L1**L2**|*F0**F1**F2*|*R0**R1**F2*|*B0**B1**B2*|
 |*************|************|************|************|
 |*L3**L4**L5**|*F3**F4**F5*|*R3**R4**R5*|*B3**B4**B5*|
 |*************|************|************|************|
 |*L6**L7**L8**|*F6**F7**F8*|*R6**R7**R8*|*B6**B7**B8*|
 |*************|************|************|************|
 |****D=5*****|
 |*D0**D1**D2*|
 |************|
 |*D3**D4**D5*|
 |************|
 |*D6**D7**D8*|
 |************|
 */

/**
 *
 * Number the cubies and the slots they go in:
 Top 		 Middle 		Bottom
 |  0      1  |				|  4      5  |
 Corners
 |  3      2  |				|  7      6  |



 |     9      |	5		1	|     8      |
 Edges	 |  6      2  |				|  4      0  |
 |     10     |	7		3	|     11     |


 // A solved cube  {"UF", "UR", "UB", "UL", "DF", "DR", "DB", "DL", "FR", "FL", "BR", "BL", "UFR", "URB", "UBL", "ULF", "DRF", "DFL", "DLB", "DBR"};
 GET FOLLOWING
 |     2      |	11		10	|     6      |
 Edges	 |  3      1  |				|  7      5  |
 |     0      |	9		8	|     4      |

 Top 		 Middle 		Bottom
 |  14      13  |				|  18      19  |
 Corners
 |  15      12  |				|  17      16  |
 //--- Specification of the 4-edges cycles the six sides cause.

 */


/**
 * 	int cycles[][] = { { 0, 1, 2, 3 }, // u
 { 4, 5, 6, 7 }, // d
 { 3, 11, 7, 10 }, // f
 { 1, 8, 5, 9 }, // b
 { 0, 8, 4, 11 }, // r
 { 2, 9, 6, 10 } // l
 };
 *
 *            |****U=0*****|
 |*U0**U1**U2*|
 |************|
 |*U3**U4**U5*|
 |************|
 |*U6**U7**U8*|
 |************|
 |****L=5******|****F=2*****|****R=4*****|****B=3*****|
 |*L0**L1**L2**|*F0**F1**F2*|*R0**R1**F2*|*B0**B1**B2*|
 |*************|************|************|************|
 |*L3**L4**L5**|*F3**F4**F5*|*R3**R4**R5*|*B3**B4**B5*|
 |*************|************|************|************|
 |*L6**L7**L8**|*F6**F7**F8*|*R6**R7**R8*|*B6**B7**B8*|
 |*************|************|************|************|
 |****D=1*****|
 |*D0**D1**D2*|
 |************|
 |*D3**D4**D5*|
 |************|
 |*D6**D7**D8*|
 |************|
 */

    /**
     * Number the cubies and the slots they go in:
     * Top 		 Middle 		Bottom
     * |  0      1  |				|  4      5  |
     * Corners
     * |  3      2  |				|  7      6  |
     * <p>
     * <p>
     * <p>
     * |     0      |	11		8	|     4      |
     * Edges	 |  3      1  |				|  7      5  |
     * |     2      |	10		9	|     6      |
     * <p>
     * <p>
     * or
     * |     0      |	8		11	|     4      |
     * Edges	|  1      3  |				|  5      7  |
     * |     2      |	9		10	|     6      |
     * <p>
     * //--- Specification of the 4-edges cycles the six sides cause.
     */


    public static void U1() {
        RubiksColor[U][0] = RubiksColorUpdate[U][6];
        RubiksColor[U][1] = RubiksColorUpdate[U][3];
        RubiksColor[U][2] = RubiksColorUpdate[U][0];
        RubiksColor[U][3] = RubiksColorUpdate[U][7];
        RubiksColor[U][5] = RubiksColorUpdate[U][1];
        RubiksColor[U][6] = RubiksColorUpdate[U][8];
        RubiksColor[U][7] = RubiksColorUpdate[U][5];
        RubiksColor[U][8] = RubiksColorUpdate[U][2];

        RubiksColor[F][0] = RubiksColorUpdate[R][0];
        RubiksColor[F][1] = RubiksColorUpdate[R][1];
        RubiksColor[F][2] = RubiksColorUpdate[R][2];

        RubiksColor[R][0] = RubiksColorUpdate[B][0];
        RubiksColor[R][1] = RubiksColorUpdate[B][1];
        RubiksColor[R][2] = RubiksColorUpdate[B][2];

        RubiksColor[B][0] = RubiksColorUpdate[L][0];
        RubiksColor[B][1] = RubiksColorUpdate[L][1];
        RubiksColor[B][2] = RubiksColorUpdate[L][2];

        RubiksColor[L][0] = RubiksColorUpdate[F][0];
        RubiksColor[L][1] = RubiksColorUpdate[F][1];
        RubiksColor[L][2] = RubiksColorUpdate[F][2];
    }

    public static void D1() {
        RubiksColor[D][0] = RubiksColorUpdate[D][6];
        RubiksColor[D][1] = RubiksColorUpdate[D][3];
        RubiksColor[D][2] = RubiksColorUpdate[D][0];
        RubiksColor[D][3] = RubiksColorUpdate[D][7];
        RubiksColor[D][5] = RubiksColorUpdate[D][1];
        RubiksColor[D][6] = RubiksColorUpdate[D][8];
        RubiksColor[D][7] = RubiksColorUpdate[D][5];
        RubiksColor[D][8] = RubiksColorUpdate[D][2];

        RubiksColor[L][6] = RubiksColorUpdate[B][6];
        RubiksColor[L][7] = RubiksColorUpdate[B][7];
        RubiksColor[L][8] = RubiksColorUpdate[B][8];

        RubiksColor[F][6] = RubiksColorUpdate[L][6];
        RubiksColor[F][7] = RubiksColorUpdate[L][7];
        RubiksColor[F][8] = RubiksColorUpdate[L][8];

        RubiksColor[R][6] = RubiksColorUpdate[F][6];
        RubiksColor[R][7] = RubiksColorUpdate[F][7];
        RubiksColor[R][8] = RubiksColorUpdate[F][8];

        RubiksColor[B][6] = RubiksColorUpdate[R][6];
        RubiksColor[B][7] = RubiksColorUpdate[R][7];
        RubiksColor[B][8] = RubiksColorUpdate[R][8];
    }

    public static void F1() {
        RubiksColor[F][0] = RubiksColorUpdate[F][6];
        RubiksColor[F][1] = RubiksColorUpdate[F][3];
        RubiksColor[F][2] = RubiksColorUpdate[F][0];
        RubiksColor[F][3] = RubiksColorUpdate[F][7];
        RubiksColor[F][5] = RubiksColorUpdate[F][1];
        RubiksColor[F][6] = RubiksColorUpdate[F][8];
        RubiksColor[F][7] = RubiksColorUpdate[F][5];
        RubiksColor[F][8] = RubiksColorUpdate[F][2];

        RubiksColor[U][6] = RubiksColorUpdate[L][8];
        RubiksColor[U][7] = RubiksColorUpdate[L][5];
        RubiksColor[U][8] = RubiksColorUpdate[L][2];

        RubiksColor[R][0] = RubiksColorUpdate[U][6];
        RubiksColor[R][3] = RubiksColorUpdate[U][7];
        RubiksColor[R][6] = RubiksColorUpdate[U][8];

        RubiksColor[D][0] = RubiksColorUpdate[R][6];
        RubiksColor[D][1] = RubiksColorUpdate[R][3];
        RubiksColor[D][2] = RubiksColorUpdate[R][0];

        RubiksColor[L][2] = RubiksColorUpdate[D][0];
        RubiksColor[L][5] = RubiksColorUpdate[D][1];
        RubiksColor[L][8] = RubiksColorUpdate[D][2];
    }

    public static void B1() {

        RubiksColor[B][0] = RubiksColorUpdate[B][6];
        RubiksColor[B][1] = RubiksColorUpdate[B][3];
        RubiksColor[B][2] = RubiksColorUpdate[B][0];
        RubiksColor[B][3] = RubiksColorUpdate[B][7];
        RubiksColor[B][5] = RubiksColorUpdate[B][1];
        RubiksColor[B][6] = RubiksColorUpdate[B][8];
        RubiksColor[B][7] = RubiksColorUpdate[B][5];
        RubiksColor[B][8] = RubiksColorUpdate[B][2];

        RubiksColor[U][0] = RubiksColorUpdate[R][2];
        RubiksColor[U][1] = RubiksColorUpdate[R][5];
        RubiksColor[U][2] = RubiksColorUpdate[R][8];

        RubiksColor[R][2] = RubiksColorUpdate[D][8];
        RubiksColor[R][5] = RubiksColorUpdate[D][7];
        RubiksColor[R][8] = RubiksColorUpdate[D][6];

        RubiksColor[D][6] = RubiksColorUpdate[L][0];
        RubiksColor[D][7] = RubiksColorUpdate[L][3];
        RubiksColor[D][8] = RubiksColorUpdate[L][6];

        RubiksColor[L][0] = RubiksColorUpdate[U][2];
        RubiksColor[L][3] = RubiksColorUpdate[U][1];
        RubiksColor[L][6] = RubiksColorUpdate[U][0];
    }

    public static void R1() {

        RubiksColor[R][0] = RubiksColorUpdate[R][6];
        RubiksColor[R][1] = RubiksColorUpdate[R][3];
        RubiksColor[R][2] = RubiksColorUpdate[R][0];
        RubiksColor[R][3] = RubiksColorUpdate[R][7];
        RubiksColor[R][5] = RubiksColorUpdate[R][1];
        RubiksColor[R][6] = RubiksColorUpdate[R][8];
        RubiksColor[R][7] = RubiksColorUpdate[R][5];
        RubiksColor[R][8] = RubiksColorUpdate[R][2];

        RubiksColor[U][2] = RubiksColorUpdate[F][2];
        RubiksColor[U][5] = RubiksColorUpdate[F][5];
        RubiksColor[U][8] = RubiksColorUpdate[F][8];

        RubiksColor[F][2] = RubiksColorUpdate[D][2];
        RubiksColor[F][5] = RubiksColorUpdate[D][5];
        RubiksColor[F][8] = RubiksColorUpdate[D][8];

        RubiksColor[D][2] = RubiksColorUpdate[B][6];
        RubiksColor[D][5] = RubiksColorUpdate[B][3];
        RubiksColor[D][8] = RubiksColorUpdate[B][0];

        RubiksColor[B][0] = RubiksColorUpdate[U][8];
        RubiksColor[B][3] = RubiksColorUpdate[U][5];
        RubiksColor[B][6] = RubiksColorUpdate[U][2];
    }

    public static void L1() {

        RubiksColor[L][0] = RubiksColorUpdate[L][6];
        RubiksColor[L][1] = RubiksColorUpdate[L][3];
        RubiksColor[L][2] = RubiksColorUpdate[L][0];
        RubiksColor[L][3] = RubiksColorUpdate[L][7];
        RubiksColor[L][5] = RubiksColorUpdate[L][1];
        RubiksColor[L][6] = RubiksColorUpdate[L][8];
        RubiksColor[L][7] = RubiksColorUpdate[L][5];
        RubiksColor[L][8] = RubiksColorUpdate[L][2];

        RubiksColor[U][0] = RubiksColorUpdate[B][8];
        RubiksColor[U][3] = RubiksColorUpdate[B][5];
        RubiksColor[U][6] = RubiksColorUpdate[B][2];

        RubiksColor[F][0] = RubiksColorUpdate[U][0];
        RubiksColor[F][3] = RubiksColorUpdate[U][3];
        RubiksColor[F][6] = RubiksColorUpdate[U][6];

        RubiksColor[D][0] = RubiksColorUpdate[F][0];
        RubiksColor[D][3] = RubiksColorUpdate[F][3];
        RubiksColor[D][6] = RubiksColorUpdate[F][6];

        RubiksColor[B][8] = RubiksColorUpdate[D][0];
        RubiksColor[B][5] = RubiksColorUpdate[D][3];
        RubiksColor[B][2] = RubiksColorUpdate[D][6];
    }

    public static void U2() {
        RubiksColor[U][0] = RubiksColorUpdate[U][8];
        RubiksColor[U][1] = RubiksColorUpdate[U][7];
        RubiksColor[U][2] = RubiksColorUpdate[U][6];
        RubiksColor[U][3] = RubiksColorUpdate[U][5];
        RubiksColor[U][5] = RubiksColorUpdate[U][3];
        RubiksColor[U][6] = RubiksColorUpdate[U][2];
        RubiksColor[U][7] = RubiksColorUpdate[U][1];
        RubiksColor[U][8] = RubiksColorUpdate[U][0];

        RubiksColor[F][0] = RubiksColorUpdate[B][0];
        RubiksColor[F][1] = RubiksColorUpdate[B][1];
        RubiksColor[F][2] = RubiksColorUpdate[B][2];

        RubiksColor[R][0] = RubiksColorUpdate[L][0];
        RubiksColor[R][1] = RubiksColorUpdate[L][1];
        RubiksColor[R][2] = RubiksColorUpdate[L][2];

        RubiksColor[B][0] = RubiksColorUpdate[F][0];
        RubiksColor[B][1] = RubiksColorUpdate[F][1];
        RubiksColor[B][2] = RubiksColorUpdate[F][2];

        RubiksColor[L][0] = RubiksColorUpdate[R][0];
        RubiksColor[L][1] = RubiksColorUpdate[R][1];
        RubiksColor[L][2] = RubiksColorUpdate[R][2];
    }

    public static void D2() {
        RubiksColor[D][0] = RubiksColorUpdate[D][8];
        RubiksColor[D][1] = RubiksColorUpdate[D][7];
        RubiksColor[D][2] = RubiksColorUpdate[D][6];
        RubiksColor[D][3] = RubiksColorUpdate[D][5];
        RubiksColor[D][5] = RubiksColorUpdate[D][3];
        RubiksColor[D][6] = RubiksColorUpdate[D][2];
        RubiksColor[D][7] = RubiksColorUpdate[D][1];
        RubiksColor[D][8] = RubiksColorUpdate[D][0];

        RubiksColor[L][6] = RubiksColorUpdate[R][6];
        RubiksColor[L][7] = RubiksColorUpdate[R][7];
        RubiksColor[L][8] = RubiksColorUpdate[R][8];

        RubiksColor[F][6] = RubiksColorUpdate[B][6];
        RubiksColor[F][7] = RubiksColorUpdate[B][7];
        RubiksColor[F][8] = RubiksColorUpdate[B][8];

        RubiksColor[R][6] = RubiksColorUpdate[L][6];
        RubiksColor[R][7] = RubiksColorUpdate[L][7];
        RubiksColor[R][8] = RubiksColorUpdate[L][8];

        RubiksColor[B][6] = RubiksColorUpdate[F][6];
        RubiksColor[B][7] = RubiksColorUpdate[F][7];
        RubiksColor[B][8] = RubiksColorUpdate[F][8];
    }

    public static void F2() {

        RubiksColor[F][0] = RubiksColorUpdate[F][8];
        RubiksColor[F][1] = RubiksColorUpdate[F][7];
        RubiksColor[F][2] = RubiksColorUpdate[F][6];
        RubiksColor[F][3] = RubiksColorUpdate[F][5];
        RubiksColor[F][5] = RubiksColorUpdate[F][3];
        RubiksColor[F][6] = RubiksColorUpdate[F][2];
        RubiksColor[F][7] = RubiksColorUpdate[F][1];
        RubiksColor[F][8] = RubiksColorUpdate[F][0];

        RubiksColor[U][6] = RubiksColorUpdate[D][2];
        RubiksColor[U][7] = RubiksColorUpdate[D][1];
        RubiksColor[U][8] = RubiksColorUpdate[D][0];

        RubiksColor[R][0] = RubiksColorUpdate[L][8];
        RubiksColor[R][3] = RubiksColorUpdate[L][5];
        RubiksColor[R][6] = RubiksColorUpdate[L][2];

        RubiksColor[D][0] = RubiksColorUpdate[U][8];
        RubiksColor[D][1] = RubiksColorUpdate[U][7];
        RubiksColor[D][2] = RubiksColorUpdate[U][6];

        RubiksColor[L][8] = RubiksColorUpdate[R][0];
        RubiksColor[L][5] = RubiksColorUpdate[R][3];
        RubiksColor[L][2] = RubiksColorUpdate[R][6];
    }

    public static void B2() {
        RubiksColor[B][0] = RubiksColorUpdate[B][8];
        RubiksColor[B][1] = RubiksColorUpdate[B][7];
        RubiksColor[B][2] = RubiksColorUpdate[B][6];
        RubiksColor[B][3] = RubiksColorUpdate[B][5];
        RubiksColor[B][5] = RubiksColorUpdate[B][3];
        RubiksColor[B][6] = RubiksColorUpdate[B][2];
        RubiksColor[B][7] = RubiksColorUpdate[B][1];
        RubiksColor[B][8] = RubiksColorUpdate[B][0];

        RubiksColor[U][0] = RubiksColorUpdate[D][8];
        RubiksColor[U][1] = RubiksColorUpdate[D][7];
        RubiksColor[U][2] = RubiksColorUpdate[D][6];

        RubiksColor[R][2] = RubiksColorUpdate[L][6];
        RubiksColor[R][5] = RubiksColorUpdate[L][3];
        RubiksColor[R][8] = RubiksColorUpdate[L][0];

        RubiksColor[D][6] = RubiksColorUpdate[U][2];
        RubiksColor[D][7] = RubiksColorUpdate[U][1];
        RubiksColor[D][8] = RubiksColorUpdate[U][0];

        RubiksColor[L][6] = RubiksColorUpdate[R][2];
        RubiksColor[L][3] = RubiksColorUpdate[R][5];
        RubiksColor[L][0] = RubiksColorUpdate[R][8];
    }

    public static void L2() {

        RubiksColor[L][0] = RubiksColorUpdate[L][8];
        RubiksColor[L][1] = RubiksColorUpdate[L][7];
        RubiksColor[L][2] = RubiksColorUpdate[L][6];
        RubiksColor[L][3] = RubiksColorUpdate[L][5];
        RubiksColor[L][5] = RubiksColorUpdate[L][3];
        RubiksColor[L][6] = RubiksColorUpdate[L][2];
        RubiksColor[L][7] = RubiksColorUpdate[L][1];
        RubiksColor[L][8] = RubiksColorUpdate[L][0];

        RubiksColor[U][0] = RubiksColorUpdate[D][0];
        RubiksColor[U][3] = RubiksColorUpdate[D][3];
        RubiksColor[U][6] = RubiksColorUpdate[D][6];

        RubiksColor[F][0] = RubiksColorUpdate[B][8];
        RubiksColor[F][3] = RubiksColorUpdate[B][5];
        RubiksColor[F][6] = RubiksColorUpdate[B][2];

        RubiksColor[D][0] = RubiksColorUpdate[U][0];
        RubiksColor[D][3] = RubiksColorUpdate[U][3];
        RubiksColor[D][6] = RubiksColorUpdate[U][6];

        RubiksColor[B][8] = RubiksColorUpdate[F][0];
        RubiksColor[B][5] = RubiksColorUpdate[F][3];
        RubiksColor[B][2] = RubiksColorUpdate[F][6];
    }

    public static void R2() {
        RubiksColor[R][0] = RubiksColorUpdate[R][8];
        RubiksColor[R][1] = RubiksColorUpdate[R][7];
        RubiksColor[R][2] = RubiksColorUpdate[R][6];
        RubiksColor[R][3] = RubiksColorUpdate[R][5];
        RubiksColor[R][5] = RubiksColorUpdate[R][3];
        RubiksColor[R][6] = RubiksColorUpdate[R][2];
        RubiksColor[R][7] = RubiksColorUpdate[R][1];
        RubiksColor[R][8] = RubiksColorUpdate[R][0];


        RubiksColor[U][2] = RubiksColorUpdate[D][2];
        RubiksColor[U][5] = RubiksColorUpdate[D][5];
        RubiksColor[U][8] = RubiksColorUpdate[D][8];

        RubiksColor[D][2] = RubiksColorUpdate[U][2];
        RubiksColor[D][5] = RubiksColorUpdate[U][5];
        RubiksColor[D][8] = RubiksColorUpdate[U][8];

        RubiksColor[F][2] = RubiksColorUpdate[B][6];
        RubiksColor[F][5] = RubiksColorUpdate[B][3];
        RubiksColor[F][8] = RubiksColorUpdate[B][0];

        RubiksColor[B][0] = RubiksColorUpdate[F][8];
        RubiksColor[B][3] = RubiksColorUpdate[F][5];
        RubiksColor[B][6] = RubiksColorUpdate[F][2];
    }

    public static void U3() {
        RubiksColor[U][0] = RubiksColorUpdate[U][2];
        RubiksColor[U][1] = RubiksColorUpdate[U][5];
        RubiksColor[U][2] = RubiksColorUpdate[U][8];
        RubiksColor[U][3] = RubiksColorUpdate[U][1];
        RubiksColor[U][5] = RubiksColorUpdate[U][7];
        RubiksColor[U][6] = RubiksColorUpdate[U][0];
        RubiksColor[U][7] = RubiksColorUpdate[U][3];
        RubiksColor[U][8] = RubiksColorUpdate[U][6];

        RubiksColor[F][0] = RubiksColorUpdate[L][0];
        RubiksColor[F][1] = RubiksColorUpdate[L][1];
        RubiksColor[F][2] = RubiksColorUpdate[L][2];

        RubiksColor[R][0] = RubiksColorUpdate[F][0];
        RubiksColor[R][1] = RubiksColorUpdate[F][1];
        RubiksColor[R][2] = RubiksColorUpdate[F][2];

        RubiksColor[B][0] = RubiksColorUpdate[R][0];
        RubiksColor[B][1] = RubiksColorUpdate[R][1];
        RubiksColor[B][2] = RubiksColorUpdate[R][2];

        RubiksColor[L][0] = RubiksColorUpdate[B][0];
        RubiksColor[L][1] = RubiksColorUpdate[B][1];
        RubiksColor[L][2] = RubiksColorUpdate[B][2];
    }

    public static void D3() {
        RubiksColor[D][0] = RubiksColorUpdate[D][2];
        RubiksColor[D][1] = RubiksColorUpdate[D][5];
        RubiksColor[D][2] = RubiksColorUpdate[D][8];
        RubiksColor[D][3] = RubiksColorUpdate[D][1];
        RubiksColor[D][5] = RubiksColorUpdate[D][7];
        RubiksColor[D][6] = RubiksColorUpdate[D][0];
        RubiksColor[D][7] = RubiksColorUpdate[D][3];
        RubiksColor[D][8] = RubiksColorUpdate[D][6];

        RubiksColor[L][6] = RubiksColorUpdate[F][6];
        RubiksColor[L][7] = RubiksColorUpdate[F][7];
        RubiksColor[L][8] = RubiksColorUpdate[F][8];

        RubiksColor[F][6] = RubiksColorUpdate[R][6];
        RubiksColor[F][7] = RubiksColorUpdate[R][7];
        RubiksColor[F][8] = RubiksColorUpdate[R][8];

        RubiksColor[R][6] = RubiksColorUpdate[B][6];
        RubiksColor[R][7] = RubiksColorUpdate[B][7];
        RubiksColor[R][8] = RubiksColorUpdate[B][8];

        RubiksColor[B][6] = RubiksColorUpdate[L][6];
        RubiksColor[B][7] = RubiksColorUpdate[L][7];
        RubiksColor[B][8] = RubiksColorUpdate[L][8];
    }

    public static void F3() {
        RubiksColor[F][0] = RubiksColorUpdate[F][2];
        RubiksColor[F][1] = RubiksColorUpdate[F][5];
        RubiksColor[F][2] = RubiksColorUpdate[F][8];
        RubiksColor[F][3] = RubiksColorUpdate[F][1];
        RubiksColor[F][5] = RubiksColorUpdate[F][7];
        RubiksColor[F][6] = RubiksColorUpdate[F][0];
        RubiksColor[F][7] = RubiksColorUpdate[F][3];
        RubiksColor[F][8] = RubiksColorUpdate[F][6];

        RubiksColor[U][6] = RubiksColorUpdate[R][0];
        RubiksColor[U][7] = RubiksColorUpdate[R][3];
        RubiksColor[U][8] = RubiksColorUpdate[R][6];

        RubiksColor[R][0] = RubiksColorUpdate[D][2];
        RubiksColor[R][3] = RubiksColorUpdate[D][1];
        RubiksColor[R][6] = RubiksColorUpdate[D][0];

        RubiksColor[D][0] = RubiksColorUpdate[L][2];
        RubiksColor[D][1] = RubiksColorUpdate[L][5];
        RubiksColor[D][2] = RubiksColorUpdate[L][8];

        RubiksColor[L][8] = RubiksColorUpdate[U][6];
        RubiksColor[L][5] = RubiksColorUpdate[U][7];
        RubiksColor[L][2] = RubiksColorUpdate[U][8];
    }

    public static void B3() {
        RubiksColor[B][0] = RubiksColorUpdate[B][2];
        RubiksColor[B][1] = RubiksColorUpdate[B][5];
        RubiksColor[B][2] = RubiksColorUpdate[B][8];
        RubiksColor[B][3] = RubiksColorUpdate[B][1];
        RubiksColor[B][5] = RubiksColorUpdate[B][7];
        RubiksColor[B][6] = RubiksColorUpdate[B][0];
        RubiksColor[B][7] = RubiksColorUpdate[B][3];
        RubiksColor[B][8] = RubiksColorUpdate[B][6];

        RubiksColor[U][0] = RubiksColorUpdate[L][6];
        RubiksColor[U][1] = RubiksColorUpdate[L][3];
        RubiksColor[U][2] = RubiksColorUpdate[L][0];

        RubiksColor[R][2] = RubiksColorUpdate[U][0];
        RubiksColor[R][5] = RubiksColorUpdate[U][1];
        RubiksColor[R][8] = RubiksColorUpdate[U][2];

        RubiksColor[D][6] = RubiksColorUpdate[R][8];
        RubiksColor[D][7] = RubiksColorUpdate[R][5];
        RubiksColor[D][8] = RubiksColorUpdate[R][2];

        RubiksColor[L][0] = RubiksColorUpdate[D][6];
        RubiksColor[L][3] = RubiksColorUpdate[D][7];
        RubiksColor[L][6] = RubiksColorUpdate[D][8];
    }

    public static void R3() {
        RubiksColor[R][0] = RubiksColorUpdate[R][2];
        RubiksColor[R][1] = RubiksColorUpdate[R][5];
        RubiksColor[R][2] = RubiksColorUpdate[R][8];
        RubiksColor[R][3] = RubiksColorUpdate[R][1];
        RubiksColor[R][5] = RubiksColorUpdate[R][7];
        RubiksColor[R][6] = RubiksColorUpdate[R][0];
        RubiksColor[R][7] = RubiksColorUpdate[R][3];
        RubiksColor[R][8] = RubiksColorUpdate[R][6];

        RubiksColor[U][2] = RubiksColorUpdate[B][6];
        RubiksColor[U][5] = RubiksColorUpdate[B][3];
        RubiksColor[U][8] = RubiksColorUpdate[B][0];

        RubiksColor[B][0] = RubiksColorUpdate[D][8];
        RubiksColor[B][3] = RubiksColorUpdate[D][5];
        RubiksColor[B][6] = RubiksColorUpdate[D][2];

        RubiksColor[D][2] = RubiksColorUpdate[F][2];
        RubiksColor[D][5] = RubiksColorUpdate[F][5];
        RubiksColor[D][8] = RubiksColorUpdate[F][8];

        RubiksColor[F][2] = RubiksColorUpdate[U][2];
        RubiksColor[F][5] = RubiksColorUpdate[U][5];
        RubiksColor[F][8] = RubiksColorUpdate[U][8];


    }

    public static void L3() {
        RubiksColor[L][0] = RubiksColorUpdate[L][2];
        RubiksColor[L][1] = RubiksColorUpdate[L][5];
        RubiksColor[L][2] = RubiksColorUpdate[L][8];
        RubiksColor[L][3] = RubiksColorUpdate[L][1];
        RubiksColor[L][5] = RubiksColorUpdate[L][7];
        RubiksColor[L][6] = RubiksColorUpdate[L][0];
        RubiksColor[L][7] = RubiksColorUpdate[L][3];
        RubiksColor[L][8] = RubiksColorUpdate[L][6];

        RubiksColor[U][0] = RubiksColorUpdate[F][0];
        RubiksColor[U][3] = RubiksColorUpdate[F][3];
        RubiksColor[U][6] = RubiksColorUpdate[F][6];

        RubiksColor[F][0] = RubiksColorUpdate[D][0];
        RubiksColor[F][3] = RubiksColorUpdate[D][3];
        RubiksColor[F][6] = RubiksColorUpdate[D][6];

        RubiksColor[D][0] = RubiksColorUpdate[B][8];
        RubiksColor[D][3] = RubiksColorUpdate[B][5];
        RubiksColor[D][6] = RubiksColorUpdate[B][2];

        RubiksColor[B][2] = RubiksColorUpdate[U][6];
        RubiksColor[B][5] = RubiksColorUpdate[U][3];
        RubiksColor[B][8] = RubiksColorUpdate[U][0];
    }

    /*
     * make a random Rubik cube by turning random side a random degree( 90 x n)
     * @param top
     * @param bottom
     * @param front
     * @param back
     * @param right
     * @param left
     */
    public String generate_random_rubik() { //delete static

        Random rand = new Random();    //rand.nextInt(n): Generating a number >=0 and <n;  Returns random int >= 0 and < n
        int n, j;
        //		srand((unsigned)time(NULL)); //be sure the results is random after run each time
        /**
         * first we have a solved Rubik Cube. then we make a random Rubik cube which we need to solve later
         */
        SolvedRubiksColor();

        String generateRandomRubikString = "";

        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            U1();
            generateRandomRubikString = generateRandomRubikString + "U1  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            D3();
            generateRandomRubikString = generateRandomRubikString + "D3  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            U2();
            generateRandomRubikString = generateRandomRubikString + "U2  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            D2();
            generateRandomRubikString = generateRandomRubikString + "D2  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            U3();
            generateRandomRubikString = generateRandomRubikString + "U3  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            U3();
            generateRandomRubikString = generateRandomRubikString + "U3  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            R1();
            generateRandomRubikString = generateRandomRubikString + "R1  ";
        }
        updateOldRubiksColor();

        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            L3();
            generateRandomRubikString = generateRandomRubikString + "L3 ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            R2();
            generateRandomRubikString = generateRandomRubikString + "R2  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            B2();
            generateRandomRubikString = generateRandomRubikString + "B2  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            R3();
            generateRandomRubikString = generateRandomRubikString + "R3  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            L1();
            generateRandomRubikString = generateRandomRubikString + "L1 ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            F1();
            generateRandomRubikString = generateRandomRubikString + "F1  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            B3();
            generateRandomRubikString = generateRandomRubikString + "B3  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            F2();
            generateRandomRubikString = generateRandomRubikString + "F2  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            B2();
            generateRandomRubikString = generateRandomRubikString + "B2  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            F3();
            generateRandomRubikString = generateRandomRubikString + "F3  ";
        }

        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            L2();
            generateRandomRubikString = generateRandomRubikString + "L2  ";
        }

        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            B1();
            generateRandomRubikString = generateRandomRubikString + "B1  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            U1();
            generateRandomRubikString = generateRandomRubikString + "U1  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            D3();
            generateRandomRubikString = generateRandomRubikString + "D3  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            U2();
            generateRandomRubikString = generateRandomRubikString + "U2  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            D2();
            generateRandomRubikString = generateRandomRubikString + "D2  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            U3();
            generateRandomRubikString = generateRandomRubikString + "U3  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            U3();
            generateRandomRubikString = generateRandomRubikString + "U3  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            R1();
            generateRandomRubikString = generateRandomRubikString + "R1  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            L3();
            generateRandomRubikString = generateRandomRubikString + "L3 ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            R2();
            generateRandomRubikString = generateRandomRubikString + "R2  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            B2();
            generateRandomRubikString = generateRandomRubikString + "B2  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            R3();
            generateRandomRubikString = generateRandomRubikString + "R3  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            L1();
            generateRandomRubikString = generateRandomRubikString + "L1 ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            F1();
            generateRandomRubikString = generateRandomRubikString + "F1  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            B3();
            generateRandomRubikString = generateRandomRubikString + "B3  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            F2();
            generateRandomRubikString = generateRandomRubikString + "F2  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            B2();
            generateRandomRubikString = generateRandomRubikString + "B2  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            F3();
            generateRandomRubikString = generateRandomRubikString + "F3  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            B1();
            generateRandomRubikString = generateRandomRubikString + "B1  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            U1();
            generateRandomRubikString = generateRandomRubikString + "U1  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            D3();
            generateRandomRubikString = generateRandomRubikString + "D3  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            U2();
            generateRandomRubikString = generateRandomRubikString + "U2  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            D2();
            generateRandomRubikString = generateRandomRubikString + "D2  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            U3();
            generateRandomRubikString = generateRandomRubikString + "U3  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            U3();
            generateRandomRubikString = generateRandomRubikString + "U3  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            R1();
            generateRandomRubikString = generateRandomRubikString + "R1  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            L3();
            generateRandomRubikString = generateRandomRubikString + "L3 ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            R2();
            generateRandomRubikString = generateRandomRubikString + "R2  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            B2();
            generateRandomRubikString = generateRandomRubikString + "B2  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            R3();
            generateRandomRubikString = generateRandomRubikString + "R3  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            L1();
            generateRandomRubikString = generateRandomRubikString + "L1 ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            F1();
            generateRandomRubikString = generateRandomRubikString + "F1  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            B3();
            generateRandomRubikString = generateRandomRubikString + "B3  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            F2();
            generateRandomRubikString = generateRandomRubikString + "F2  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            B2();
            generateRandomRubikString = generateRandomRubikString + "B2  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            F3();
            generateRandomRubikString = generateRandomRubikString + "F3  ";
        }
        updateOldRubiksColor();
        n = (rand.nextInt(4));
        for (j = 0; j < n; j++) {
            B1();
            generateRandomRubikString = generateRandomRubikString + "B1  ";
        }
        updateOldRubiksColor();

        String solvedStringTop = new String(RubiksColor[mainU]);
        String solvedStringBottom = new String(RubiksColor[mainD]);
        String solvedStringFront = new String(RubiksColor[mainF]);
        String solvedStringBack = new String(RubiksColor[mainB]);
        String solvedStringRight = new String(RubiksColor[mainR]);
        String solvedStringLeft = new String(RubiksColor[mainL]);

        String randomRubikString = solvedStringTop + solvedStringBottom + solvedStringFront + solvedStringBack + solvedStringRight + solvedStringLeft;

        return randomRubikString;

//		System.out.println(generateRandomRubikString);
    }


    public static String generateRubikInputString() {

        String CubikCubeShort = "";

        // follow the order of 	a solved cube : edges  {"UF", "UR", "UB", "UL", "DF", "DR", "DB", "DL", "FR", "FL", "BR", "BL",}
        CubikCubeShort = CubikCubeShort + RubiksColor[U][7] + RubiksColor[F][1] + " ";
        CubikCubeShort = CubikCubeShort + RubiksColor[U][5] + RubiksColor[R][1] + " ";
        CubikCubeShort = CubikCubeShort + RubiksColor[U][1] + RubiksColor[B][1] + " ";
        CubikCubeShort = CubikCubeShort + RubiksColor[U][3] + RubiksColor[L][1] + " ";

        CubikCubeShort = CubikCubeShort + RubiksColor[D][1] + RubiksColor[F][7] + " ";
        CubikCubeShort = CubikCubeShort + RubiksColor[D][5] + RubiksColor[R][7] + " ";
        CubikCubeShort = CubikCubeShort + RubiksColor[D][7] + RubiksColor[B][7] + " ";
        CubikCubeShort = CubikCubeShort + RubiksColor[D][3] + RubiksColor[L][7] + " ";

        CubikCubeShort = CubikCubeShort + RubiksColor[F][5] + RubiksColor[R][3] + " ";
        CubikCubeShort = CubikCubeShort + RubiksColor[F][3] + RubiksColor[L][5] + " ";
        CubikCubeShort = CubikCubeShort + RubiksColor[B][3] + RubiksColor[R][5] + " ";
        CubikCubeShort = CubikCubeShort + RubiksColor[B][5] + RubiksColor[L][3] + " ";

        // follow the order of a solved cube : corners  {"UFR", "URB", "UBL", "ULF", "DRF", "DFL", "DLB", "DBR"};
        CubikCubeShort = CubikCubeShort + RubiksColor[U][8] + RubiksColor[F][2] + RubiksColor[R][0] + " ";
        CubikCubeShort = CubikCubeShort + RubiksColor[U][2] + RubiksColor[R][2] + RubiksColor[B][0] + " ";
        CubikCubeShort = CubikCubeShort + RubiksColor[U][0] + RubiksColor[B][2] + RubiksColor[L][0] + " ";
        CubikCubeShort = CubikCubeShort + RubiksColor[U][6] + RubiksColor[L][2] + RubiksColor[F][0] + " ";

        CubikCubeShort = CubikCubeShort + RubiksColor[D][2] + RubiksColor[R][6] + RubiksColor[F][8] + " ";
        CubikCubeShort = CubikCubeShort + RubiksColor[D][0] + RubiksColor[F][6] + RubiksColor[L][8] + " ";
        CubikCubeShort = CubikCubeShort + RubiksColor[D][6] + RubiksColor[L][6] + RubiksColor[B][8] + " ";
        CubikCubeShort = CubikCubeShort + RubiksColor[D][8] + RubiksColor[B][6] + RubiksColor[R][8];

        System.out.println(CubikCubeShort);
        return CubikCubeShort;
    }


    static void updateOldRubiksColor() {
        int i, j;

        for (i = 0; i < 6; i++) {
            for (j = 0; j < 9; j++) {
                RubiksColorUpdate[i][j] = RubiksColor[i][j];
            }
        }
    }


}
