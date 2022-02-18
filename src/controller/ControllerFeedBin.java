package controller;

import gui.NewFeedBinGUI;

public class ControllerFeedBin implements Runnable {

    private final ModelFeedBin[] bins;

    private volatile int binNumber;
    private volatile int operation;
    private volatile String value;

    private volatile boolean orderFulfillment;
    private volatile String[] inspectionResult;

    public ControllerFeedBin(ModelFeedBin[] bins) {
        this.bins = bins;
        this.binNumber = 0;
        this.operation = -1;
    }

    private boolean setProductName(int binNumber, String newProductName) {
        return this.bins[binNumber].setProductName(newProductName);
    }

    private void flush(int binNumber) {
        this.bins[binNumber].flush();
    }

    private boolean addProduct(int binNumber, double volume) {
        return this.bins[binNumber].addProduct(volume);
    }

    private String[] inspectBin(int binNumber) {

        String binNo = String.valueOf(this.bins[binNumber].getBinNumber()); // Kind of moot, but doing it for sake of completeness
        String productName = this.bins[binNumber].getProductName();
        String maxVolume = String.valueOf(this.bins[binNumber].getMaxVolume());
        String currentVolume = String.valueOf(this.bins[binNumber].getCurrentVolume());

        return new String[] {binNo, productName, maxVolume, currentVolume};

    }

    public void issueOrder(int binNumber, int operation, String value) {
        this.binNumber = binNumber;
        this.operation = operation;
        this.value = value;
    }

    public boolean isOrderFulFilled() {
        return orderFulfillment;
    }

    public String[] getInspectionResult() {
        return inspectionResult;
    }

    @Override
    public void run() {

        while (NewFeedBinGUI.controllerLatch.getCount() > 0) {

            if (operation > -1) {

                switch (operation) {

                    case 0:

                        this.orderFulfillment = setProductName(binNumber, value);
                        break;

                    case 1:

                        flush(binNumber);
                        break;

                    case 2:

                        // Double parse-ability is guaranteed by the front-end
                        this.orderFulfillment = addProduct(binNumber, Double.parseDouble(value));
                        break;

                    case 3:

                        this.inspectionResult = inspectBin(binNumber);
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
