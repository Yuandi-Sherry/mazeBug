package info.gridworld.maze;

import info.gridworld.actor.Actor;
import info.gridworld.actor.Bug;
import info.gridworld.actor.Flower;
import info.gridworld.grid.*;
import info.gridworld.actor.Rock;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

import javax.swing.JOptionPane;

/**
 * A <code>MazeBug</code> can find its way in a maze. <br />
 * The implementation of this class is testable on the AP CS A and AB exams.
 */
public class MazeBug extends Bug {
    public Location next;
    public Location last;
    public boolean isEnd = false;
    public Stack<ArrayList<Location>> crossLocation = new Stack<ArrayList<Location>>();
    public Integer stepCount = 0;
    boolean hasShown = false;//final message has been shown
    boolean first = true;
    int [] statistics = {1,1,1,1};

    /**
     * Constructs a maze bug that acn traces a square of a given side length
     * 
     * @param length
     *            the side length
     */
    public MazeBug() {
        setColor(Color.GREEN);
    }

    /**
     * Moves to the next location of the square.
     */
    public void act() {
        
        boolean willMove = canMove();

        if(first) {
            ArrayList<Location> temp = new ArrayList<Location>();
            temp.add(getLocation());
            crossLocation.push(temp);
            first = !first;
        }

        // 取当前栈顶节点
        if(!crossLocation.empty()) {
            ArrayList<Location> curTop = crossLocation.peek();
            // System.out.println(curTop.get(0).getRow() + " " + curTop.get(0).getCol());
            if(isEnd(curTop.get(0))) {
                // System.out.println("find red stone");
                isEnd = true;
                crossLocation.pop();            
            }
        }
        

        if (isEnd == true) {
        // to show step count when reach the goal     
            if (hasShown == false) {
                String msg = stepCount.toString() + " steps";
                JOptionPane.showMessageDialog(null, msg);
                hasShown = true;
            }
            for(int i = 0; i < 4; i++) {
                System.out.println("location:" + i + " " + statistics[i]);
            }
            return;
        }
        // 如果当前栈顶节点存在”未访问”状态的邻接节点
        else if (willMove) {

            //store valid adjacent locations of current location
            ArrayList<Location> adjLocs = getValidAdjacentLocations(getLocation()); 

            // 选择一个未访问节点
            Location a = adjLocs.get(0);
            double rand = Math.random();
            int aimDirection;
            if(rand < (double)sum(1)/sum(4)) {
                aimDirection = 0;
            }
            else if(rand < (double)sum(2)/sum(4)) {
                aimDirection = 1;
            }
            else if(rand < (double)sum(3)/sum(4)) {
                aimDirection = 2;
            }
            else
                aimDirection = 3;

            for(Location i : adjLocs) {
                if(getLocation().getDirectionToward(i) == aimDirection) {
                    a = i;
                }
            }

            // 置为”已访问”状态

            // 将它入栈
            ArrayList<Location> temp = new ArrayList<Location> ();
            temp.add(a);
            crossLocation.push(temp);
            next = a;

            // direction statistics
            statistics[getLocation().getDirectionToward(next)/90]++;
            //move to next
            move();

            //increase step count when move 
            stepCount++;
        } 
        // 如果当前栈顶节点不存在”未访问”状态的邻接节点
        else {
            crossLocation.pop();
            ArrayList<Location> curTop = crossLocation.peek();
            next = curTop.get(0);
            // direction statistics

            statistics[(getLocation().getDirectionToward(next) + 180)%360/90]--;
            //move to next
            move();
            //increase step count when move 
            stepCount++;

        }
    }

    /**
     * Find all positions that can be move to.
     * 
     * @param loc
     *            the location to detect.
     * @return List of positions.
     */
    public ArrayList<Location> getValid(Location loc) {
        Grid<Actor> gr = getGrid();
        if (gr == null)
            return null;
        ArrayList<Location> valid = new ArrayList<Location>();
        
        return valid;
    }

    /**
     * Tests whether this bug can move forward into a location that is empty or
     * contains a flower.
     * 
     * @return true if this bug can move.
     */
    public boolean canMove() {
        ArrayList<Location> posNext = getValidAdjacentLocations(getLocation());

        return posNext.size() != 0;
    }
    /**
     * Moves the bug forward, putting a flower into the location it previously
     * occupied.
     */
    public void move() {
        Grid<Actor> gr = getGrid();
        if (gr == null)
            return;
        Location loc = getLocation();
        if (gr.isValid(next)) {
            setDirection(getLocation().getDirectionToward(next));
            moveTo(next);
        } else
            removeSelfFromGrid();
        Flower flower = new Flower(getColor());
        flower.putSelfInGrid(gr, loc);
    }

    /**
     * The bug can only move to NORTH, SOUTH, EAST, WEST 4 directions
     */
    private ArrayList<Location> getAdjacentLocations(Location loc) {
        int adjustedDirection = (getDirection() + 90) % Location.FULL_CIRCLE;
        int dc = 0;
        int dr = 0;
        ArrayList<Location> ans = new ArrayList<Location>();
        for(int i = 0; i < 360; i++) {
            adjustedDirection = (getDirection() + i) % Location.FULL_CIRCLE;
            if(adjustedDirection == Location.EAST) 
                ans.add(new Location(loc.getRow(), loc.getCol() + 1));
            else if (adjustedDirection == Location.SOUTH) 
                ans.add(new Location(loc.getRow() + 1, loc.getCol()));
            else if (adjustedDirection == Location.WEST)
                ans.add(new Location(loc.getRow(), loc.getCol() - 1));
            else if (adjustedDirection == Location.NORTH)
                ans.add(new Location(loc.getRow() - 1, loc.getCol()));
        }
        // get potential next location
        return ans;
    }

    private ArrayList<Location> getValidAdjacentLocations(Location loc) {
        ArrayList<Location> posNext = getAdjacentLocations(loc);
        ArrayList<Location> posValid = new ArrayList<Location>();
        Actor neighbor;
        Grid<Actor> gr = getGrid();
        if (gr == null)
            return posValid;    
        if(posNext.size() != 0) {
            
            for(Location a : posNext) {
                if (gr.isValid(a)) {
                    neighbor = gr.get(a);
                    if((neighbor == null) || (neighbor instanceof Rock && neighbor.getColor().getRed() == Color.RED.getRed())) {
                        posValid.add(a);
                    }
                }
                
            }
        }
        return posValid;
    }
    
    //if a neighbor is red rock, it will be end. 
    private boolean isEnd(Location loc) {
        Grid<Actor> gr = getGrid();
        if (gr == null)
            return false;  
        ArrayList<Location> posNext = getValidAdjacentLocations(loc);
        for(Location a : posNext) {
            if(gr.get(a) instanceof Rock && gr.get(a).getColor().getRed() == Color.RED.getRed()) {
                return true;
            } 
        }         
        return false;
    }

    private int sum(int range) {
        int ans = 0;
        for(int i = 0 ; i < range; i++) {
            ans+= statistics[i];
        }
        return ans;
    }
}