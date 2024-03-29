package com.company;

import com.company.mail.MailService;
import com.company.utail.LongTask;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Async {

    MailService mailService = new MailService();

    private void test() {

        // Function<Void,String>f= aVoid -> "null";
        var d = mailService.sendAsync().thenApply((f) -> "null").exceptionally(throwable -> "has Error");
        //thenApply: to edit return from sendAsync
        //exceptionally: if sendAsync trow error u can return new value
        // thenAccept: take final value from get() and u can print  it
        //thenCompose: when ur future end u can start a new future


        var price1 = CompletableFuture.supplyAsync(() -> "20USD");
        var price2 = CompletableFuture.supplyAsync(() -> "30USD");
        var tax = CompletableFuture.supplyAsync(() -> 0.1);

        var taxForPrice1 = price1.thenCombine(tax, (strPrice, doubleTax) ->
                //thenCombine: run 2 CompletableFuture at the same time
                priceToInt(strPrice) * doubleTax)
                .thenAccept(System.out::println);

        var taxForAllPrice = CompletableFuture.allOf(price1, price2, tax).thenRun(() -> {
            //allOf: Like [thenCombine] put accept multi CompletableFuture
            try {
                var allPrice = priceToInt(price1.get()) + priceToInt(price2.get());
                var allTax = tax.get() * allPrice;
                System.out.println(allTax);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });


        //anyOf: take multi CompletableFuture and return value from faster
        var faster = CompletableFuture.supplyAsync(() -> 5);
        var slower = CompletableFuture.supplyAsync(() -> {
            LongTask.start();
            return 2;
        });
        var getDataFromFaster = CompletableFuture.anyOf(faster, slower).thenAccept(System.out::println);


        //completeOnTimeout: if future take over then selected timeOut return default value
        var longOperation = CompletableFuture.supplyAsync(() -> {
            LongTask.start();
            return 6;
        });
        var timeOutToLongOperation = longOperation
                .completeOnTimeout(1, 2, TimeUnit.SECONDS)
                .thenAccept(System.out::println);


    }

    private int priceToInt(String input) {
        return Integer.parseInt(input.replace("USD", ""));
    }
}
