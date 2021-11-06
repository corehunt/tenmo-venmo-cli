package com.techelevator.tenmo.model;

import java.math.BigDecimal;
import java.util.List;

public class Transfer {

    private int transferId;
    private int transferTypeId;
    private int transferStatusId;
    private int accountFrom;
    private int accountTo;
    private BigDecimal amount;

    public Transfer() {
    }

    public Transfer(int transferId, int transferTypeId, int transferStatusId, int accountFrom, int accountTo, BigDecimal amount) {
        this.transferId = transferId;
        this.transferTypeId = transferTypeId;
        this.transferStatusId = transferStatusId;
        this.accountFrom = accountFrom;
        this.accountTo = accountTo;
        this.amount = amount;
    }

    public int getTransferId() {
        return this.transferId;
    }

    public void setTransferId(int transferId) {
        this.transferId = transferId;
    }

    public int getTransferTypeId() {
        return this.transferTypeId;
    }

    public void setTransferTypeId(int transferTypeId) {
        this.transferTypeId = transferTypeId;
    }

    public int getTransferStatusId() {
        return this.transferStatusId;
    }

    public void setTransferStatusId(int transferStatusId) {
        this.transferStatusId = transferStatusId;
    }

    public int getAccountFrom() {
        return this.accountFrom;
    }

    public void setAccountFrom(int accountFrom) {
        this.accountFrom = accountFrom;
    }

    public int getAccountTo() {
        return this.accountTo;
    }

    public void setAccountTo(int accountTo) {
        this.accountTo = accountTo;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        String transferType;
        String transferStatus;
        if (transferTypeId == 2){
            transferType = "Send";
        }else{
            transferType = "Request";
        }
        if (transferStatusId == 2){
            transferStatus = "Approved";
        }else if(transferStatusId == 1){
            transferStatus = "Pending";
        }else{
            transferStatus = "Rejected";
        }

        return "-----Transfer-----" +
                "ID=" + transferId +
                ", transferType = " + transferType +
                ", transferStatus = " + transferStatus +
                ", accountFrom = " + accountFrom +
                ", accountTo = " + accountTo +
                ", amount = $" + amount + "\n";
    }
}
