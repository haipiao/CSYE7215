package hw5;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This file needs to hold your solver to be tested. 
 * You can alter the class to extend any class that extends MazeSolver.
 * It must have a constructor that takes in a Maze.
 * It must have a solve() method that returns the datatype List<Direction>
 *   which will either be a reference to a list of steps to take or will
 *   be null if the maze cannot be solved.
 */
public class StudentMTMazeSolver extends SkippingMazeSolver {
    public StudentMTMazeSolver(Maze maze)
    {
        super(maze);
    }

    public List<Direction> solve()
    {
        // TODO: Implement your code here
        // throw new RuntimeException("Not yet implemented!");
        int numProcessor = Runtime.getRuntime().availableProcessors();
        ExecutorService threadPool = Executors.newFixedThreadPool(numProcessor);

        List<Callable<List<Direction>>> tasks = new LinkedList<Callable<List<Direction>>>();

        try{
            Choice startPoints = firstChoice(maze.getStart());

            while(!startPoints.choices.isEmpty()){
                tasks.add(new DFS(follow(startPoints.at,startPoints.choices.peek()),startPoints.choices.pop()));
            }

        }catch (SolutionFound e) {
            System.out.println("Solution found.");
        }

        List<Direction> solutions = null;
        try{
            for(int i=0;i<tasks.size();i++){
                solutions = threadPool.submit(tasks.get(i)).get();
                if(solutions !=null) break;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        threadPool.shutdown();
        return solutions;
    }

    private class DFS implements Callable<List<Direction>>{
        Choice startPoint;
        Direction firstDirection;

        public DFS(Choice startPoint, Direction firstDirection){
            this.startPoint = startPoint;
            this.firstDirection = firstDirection;
        }

        @Override
        public  List<Direction> call(){
            LinkedList<Choice> choiceStack = new LinkedList<Choice>();
            Choice ch;

            try {
                choiceStack.push(this.startPoint);
                while (!choiceStack.isEmpty()) {
                    ch = choiceStack.peek();
                    if (ch.isDeadend()) {
                        // backtrack.
                        choiceStack.pop();
                        if (!choiceStack.isEmpty()) choiceStack.peek().choices.pop();
                        continue;
                    }
                    choiceStack.push(follow(ch.at, ch.choices.peek()));
                }
                // No solution found.
                return null;
            } catch (SolutionFound e) {
                Iterator<Choice> iter = choiceStack.iterator();
                LinkedList<Direction> solutionPath = new LinkedList<Direction>();
                while (iter.hasNext()) {
                    ch = iter.next();
                    solutionPath.push(ch.choices.peek());
                }

                solutionPath.push(this.firstDirection);

                if (maze.display != null)
                    maze.display.updateDisplay();

                return pathToFullPath(solutionPath);
            }
        }
    }
}
