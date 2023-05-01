package com.example.quixo.Model;
import java.util.ArrayList;
import java.util.Random;

public class PlayerAI {
    Integer playerTurn = 2 ;
    public PlayerAI(Integer playerTurn) {
        this.playerTurn = playerTurn;
    }
    public ArrayList<Position> heuristicSearch(int[][] game){
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int iMark = 0;
        int iMark2 = 0;
        Position p = null;
        Position p2 = null;
        Board board = new Board();
        board.setGame(cloneGame(game));
        ArrayList<Position> movables = board.getPlayableSources(2);// קבל את החלקים הניתנים להזזה עבור השחקן AI

        for (Integer i = 0; i < movables.size(); i++) {
            ArrayList<Position> destinations = board.getPlayableDestinations(5,movables.get(i));//מקבל את היעדים שניתנים להפעלה
            for (int j = 0; j < destinations.size(); j++ ){
                int[][] gameTemp = move(movables.get(i),destinations.get(j),game);
                int DefendBoardScore = getBoardScore(gameTemp, 1) ;
                //האלגוריתם ישאף למזער מספר זה לכן נחפש את המהלך שהופך את מספר זה לקטן ביותר.
                int AttackBoardScore = getBoardScore(gameTemp, 2);
                if(AttackBoardScore > max)
                {
                    max = AttackBoardScore;
                    iMark2 = i;
                    p2 = destinations.get(j);
                }
                if (DefendBoardScore < min)
                    {
                        // .מוצאים את המספר המינמלי של עיגול בקו אחרי ההזזה..
                        //פונקציה זאת מוצאת את כמות העיגולים הגדולה ביותר שנימצאת על איזשהו קו לאחר ביצוע המהלך ומפריע להן על ידי אותה הוזזה.
                        min = DefendBoardScore;
                        iMark = i;
                        p = destinations.get(j);
                    }
            }
        }
        if(min + 1  < max)
        {
            //הוספנו 1 למינימום מכיוון שזהו לא המצב הנוכחי, לדוגמה אם יהיה מינימום שהוא 3 זאת אומרת שבקו יש 4 שחקנים.
            //באותה דוגמה נאמר שיש לנו מקסימום של 4 שחקני, זאת אומרת שמצב הנוכחי יש 3 בקו עם הכי הרבה איקסים
            //במצב זה נתעדף את הצעד ההגנתי למרות שערך המקסימום גדול מערך המינימום
            //במילים אחרות ההפרש בין המצב האידיאלי של תיעדוף המקסימום הוא 2 לכן רק במצב זה נתעדף רק את הצעד של המינימום
            iMark = iMark2;
            p = p2;
        }
        ArrayList<Position> bestMove = new ArrayList<>();//מחזיק את הקוביה לבחירה ואת היעד להוזזה
        bestMove.add(movables.get(iMark));
        bestMove.add(p);
        return bestMove;//החזר את המהלך הטוב ביותר

    }

    //**************************************************************************//

    //פונקציה זו מקבלת את הלוח ומספר שחקן ובודקת את הניקוד של אותו השחקן על איך שהלוח נראה
    public int getBoardScore(int[][] Board, int player)  {
        //הפונקציה מחזירה את הציון המקסימלי מתוך ארבעת הציונים הללו, המייצג את הציון הכולל של השחקן על לוח המשחק.
        int diagScore = getDiagScore(Board, player);
        int diagScoreOppose = getInvDiagScore(Board, player);
        int rowScore = getMaxRowScore(Board, player);
        int colsScore = getMaxColScore(Board, player);


        int max = Integer.max(diagScore,diagScoreOppose);
        max = Integer.max(max,rowScore);
        max = Integer.max(max,colsScore);
        return max;
    }

    public int getLineScore(ArrayList<Integer> line, int player )  {
        //חישוב ניקוד שורה
        int score = 0;
        for (int i = 0; i < line.size(); i++) {
            if (line.get(i) == player) {
                score++;
            }
        }
        return score;
    }

    public int getMaxRowScore(int[][] Board, int player)  {
        //
        int size = Board.length;
        ArrayList<Integer> scores = new ArrayList<Integer>();
        for (int i = 0; i < size; i++) {
            ArrayList<Integer> row = new ArrayList<Integer>();
            //.
            for(int j=0; j< Board[i].length; j++){
                row.add(Board[i][j]);
            }
            scores.add(getLineScore(row, player));
        }
        return getMaxInArray(scores);
    }

    public int getMaxColScore(int[][] Board, int player)  {
        int size = Board.length;
        ArrayList<Integer> scores = new ArrayList<Integer>();
        for (int i = 0; i < size; i++) {
            ArrayList<Integer> cols = new ArrayList<Integer>();
            //
            for(int j=0; j< Board.length; j++){
                cols.add(Board[j][i]);
            }
            scores.add(getLineScore(cols, player));
        }
        return getMaxInArray(scores);
    }

    public int getDiagScore(int[][] Board, int player)  {
        ArrayList<Integer> diagonal = new ArrayList<>() ;
        int size = Board.length;
        for (int i = 0; i < size; i++ ){
            diagonal.add(Board[i][i]);
        }
        return getLineScore(diagonal, player);
    }

    public int getInvDiagScore(int[][] Board, int player) {
        ArrayList<Integer> diagonalInverse = new ArrayList<>() ;
        int size = Board.length;
        for (int i = 0; i < size; i++ ){
            diagonalInverse.add(Board[i][size-1-i]);
        }
        return getLineScore(diagonalInverse, player);
    }

    public int getMaxInArray(ArrayList<Integer> scoreArray)  {
        int max = 0;
        for(int i = 0; i < scoreArray.size(); i++) {
            if (scoreArray.get(i) > max) {
                max = scoreArray.get(i);
            }
        }
        return max;
    }

    public int[][] move(Position A_source, Position B_Destination, int [][] game) {
        int[][] ret = cloneGame(game);
        int XSource = A_source.getX(), YSource = A_source.getY();
        int XDest = B_Destination.getX(), YDest = B_Destination.getY();
        /* אם הם נמצאים באותה עמודה, הדבר שיש לשנות הוא הקו
           אבל יש צורך גם להזיז את כל החלקים האחרים לפי 2 מקרים */
        if (YSource == YDest) {

            //אם ליעד יש ID גדול יותר
            // אנחנו עוברים ימינה (מגדילים)
            if (XSource < XDest) {
                for (int i = XSource; i < 4; i++) {

                    ret[i][YSource] = ret[i + 1][YSource];

                }
            }
            // אם ליעד יש ID קטן יותר
            // אנחנו עוברים שמאלה (ירידה)
            if (XSource > XDest) {
                for (int i = XSource; i > 0; i--) {
                    ret[i][YSource] = ret[i - 1][YSource];

                }
            }

        }
        /* אם הם נמצאים על אותה שורה, הדבר שיש לשנות הוא העמודה
         * אבל יש צורך גם להזיז את כל החלקים האחרים לפי 2 מקרים */
        if (XSource == XDest) {
            // אם ליעד יש ID גדול יותר
            // אנחנו עוברים ימינה (מגדילים)
            if (YSource < YDest) {
                for (int i = YSource; i < 4; i++) {

                    ret[XSource][i]= ret[XSource][i + 1];

                }
            }
            // אם ליעד יש ID קטן יותר
            // אנחנו עוברים שמאלה (ירידה)
            // אנחנו עוברים שמאלה (ירידה)
            if (YSource > YDest) {
                for (int i = YSource; i > 0; i--) {
                    ret[XSource][i]= ret[XSource][i - 1];

                }
            }
        }
        ret[XDest][YDest] = 2;
        return ret;
    }

    public int [][] cloneGame(int[][] game){
        if(game == null) return null;

        int[][] result = new int[game.length][];
        for(int i=0; i< game.length; i++){
            result[i] = game[i].clone();
        }
        return result;
    }
}







