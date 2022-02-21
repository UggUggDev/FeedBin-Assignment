package supervisor;

import controller.ModelFeedBin;
import gui.NewFeedBinGUI;

import java.util.List;

public class ControllerSupervisor implements Runnable {

    private final ModelFeedBin[] bins;

    private volatile String recipe;
    private volatile String batch;
    private volatile int operation;

    private volatile boolean orderFulFillment;
    private volatile List<String[]> inspectionResults;

    public ControllerSupervisor(ModelFeedBin[] bins) {
        this.bins = bins;
        this.batch = "";
        this.operation = -1;
    }

    /*

    Operation IDs and their roles for the ControllerSupervisor:

    0 : Adding a batch (from a recipe)
    1 : Processing the next batch
    2 : Inspecting all the bins

     */
    public void issueOrder(String recipe, String batch, int operation) {
        this.recipe = recipe;
        this.batch = batch;
        this.operation = operation;
    }

    public boolean isOrderFulfilled() {
        return orderFulFillment;
    }

    public List<String[]> getInspectionResults() {
        return inspectionResults;
    }

    @Override
    public void run() {

        while (NewFeedBinGUI.controllerLatch.getCount() > 0) {

            if (operation > -1) {

                switch (operation) {

                    case 0:
                        break;

                    case 1:
                        break;

                    case 2:
                        break;

                }

                this.operation = -1;
                NewFeedBinGUI.guiLatch.countDown();

            }

            try {
                Thread.sleep(1L);
            } catch (InterruptedException e) {
                System.err.println("Error : " + getClass().getName() + " was interrupted!");
                e.printStackTrace(); // Friendly message followed by stack trace
            }

        }

        NewFeedBinGUI.exitLatch.countDown();

    }

}
