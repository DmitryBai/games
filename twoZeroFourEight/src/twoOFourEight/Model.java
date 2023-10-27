package twoOFourEight;

import java.util.*;

public class Model {
    private static final int FIELD_WIDTH = 4;
    private Tile[][] gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
    int score = 0;
   int maxTile = 0;
   private Stack<Tile[][]> previousStates = new Stack<Tile[][]>();
   private Stack<Integer> previousScores = new Stack<Integer>();
   boolean isSaveNeeded = true;

    public Model() {
        resetGameTiles();
    }

    private void saveState(Tile[][] tiles){
        Tile[][] tempTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                tempTiles[i][j] = new Tile(tiles[i][j].value);
            }
        }
        previousStates.push(tempTiles);
        previousScores.push(score);
        isSaveNeeded = false;
    }

    public void rollback(){
        if(!previousScores.isEmpty()&&!previousStates.isEmpty()){
        gameTiles = previousStates.pop();
        score = previousScores.pop();}
    }

    private List<Tile> getEmptyTiles(){
        List<Tile> emptyTiles = new ArrayList<>();
        for(int j = 0; j < FIELD_WIDTH; j++){
            for(int k = 0; k < FIELD_WIDTH; k++) if(gameTiles[j][k].isEmpty()) emptyTiles.add(gameTiles[j][k]);
        }
        return emptyTiles;

    }

    private void addTile(){
        List<Tile> tiles = getEmptyTiles();
        if(!tiles.isEmpty()){
            int index = (int) (tiles.size() * Math.random());
            Tile emptyTile = tiles.get(index);
            emptyTile.value = Math.random() < 0.9 ? 2 : 4;


        }
    }

    public void resetGameTiles(){
        for(int j = 0; j < FIELD_WIDTH; j++){
            for(int k = 0; k < FIELD_WIDTH; k++) gameTiles[j][k] = new Tile();
        }
        addTile();
        addTile();
    }

    private boolean compressTiles(Tile[] tiles){
        int insertPosition = 0;
        boolean result = false;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (!tiles[i].isEmpty()) {
                if (i != insertPosition) {
                    tiles[insertPosition] = tiles[i];
                    tiles[i] = new Tile();
                    result = true;
                }
                insertPosition++;
            }
        }
        return result;
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    public boolean canMove(){
        if(getEmptyTiles().size()!=0) return true;
        for(int j = 0; j < FIELD_WIDTH-1; j++){
            for(int k = 0; k < FIELD_WIDTH-1; k++){
                Tile tile = gameTiles[j][k];

                if(tile.value == gameTiles[j][k+1].value || tile.value == gameTiles[j+1][k].value) return true;
        }}
        return false;
    }

    private boolean mergeTiles(Tile[] tiles){
        boolean result = false;
        LinkedList<Tile> tilesList = new LinkedList<>();
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (tiles[i].isEmpty()) {
                continue;
            }

            if (i < FIELD_WIDTH - 1 && tiles[i].value == tiles[i + 1].value) {
                int updatedValue = tiles[i].value * 2;
                if (updatedValue > maxTile) {
                    maxTile = updatedValue;
                }
                score += updatedValue;
                tilesList.addLast(new Tile(updatedValue));
                tiles[i + 1].value = 0;
                result = true;
            } else {
                tilesList.addLast(new Tile(tiles[i].value));
            }
            tiles[i].value = 0;
        }

        for (int i = 0; i < tilesList.size(); i++) {
            tiles[i] = tilesList.get(i);
        }
        return result;
    }

    private Tile[][] rotateClockwise(Tile[][] tiles) {
        final int N = tiles.length;
        Tile[][] result = new Tile[N][N];
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                result[c][N - 1 - r] = tiles[r][c];
            }
        }
        return result;
    }


    public void left(){
        if(isSaveNeeded) saveState(gameTiles);
        boolean moveFlag = false;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (compressTiles(gameTiles[i]) | mergeTiles(gameTiles[i])) {
                moveFlag = true;
            }
        }
        if (moveFlag) {
            addTile();
        }
        isSaveNeeded = true;
    }

    public void up(){
        saveState(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        left();
        gameTiles = rotateClockwise(gameTiles);
    }

    public void down(){
        saveState(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        left();
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
    }

    public void right(){
        saveState(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        left();
        gameTiles = rotateClockwise(gameTiles);

        gameTiles = rotateClockwise(gameTiles);
    }

    public void randomMove(){
        int n = ((int) Math.random() *100)%4;
        switch(n){
            case 0: left(); break;
            case 1: right(); break;
            case 2: up(); break;
            case 3: down(); break;
        }
    }

    public boolean hasBoardChanged(){
        for(int j = 0; j < FIELD_WIDTH; j++){
            for(int k = 0; k < FIELD_WIDTH; k++){
                if(gameTiles[j][k].value!=previousStates.peek()[j][k].value) return true;
            }
        }
        return false;
    }

    public MoveEfficiency getMoveEfficiency(Move move){
        MoveEfficiency moveEfficiency = new MoveEfficiency(-1, 0, move);
        move.move();
        if(hasBoardChanged()){
            moveEfficiency = new MoveEfficiency(getEmptyTiles().size(), score, move);
        }
        rollback();
        return moveEfficiency;

    }

    public void autoMove(){
        PriorityQueue<MoveEfficiency> priorityQueue = new PriorityQueue(4, Collections.reverseOrder());
        priorityQueue.offer(getMoveEfficiency(this::up));
        priorityQueue.offer(getMoveEfficiency(this::down));
        priorityQueue.offer(getMoveEfficiency(this::left));
        priorityQueue.offer(getMoveEfficiency(this::right));
        priorityQueue.peek().getMove().move();

    }
}
