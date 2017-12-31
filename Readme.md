# AltCoin desktop application for Bittrex Exchange (Wallet/Bot/Stop-loss)

# AltCoin features
1. Real time wallet to display all owned coins:
* Shows percentage for every coin.
* Shows BTC equivalent.
* Shows total value of wallet.
![picture](https://github.com/BlackOfWhite/AltCoin/blob/master/images/WalletPieChart1.png)

2. Place Buy/Sell order with **stop-loss** option:
* Place normal Bittrex buy/sell orders using this application.
* You can add stop-loss option to every buy/sell order created within AltCoin application.
* Stop-loss cancels the order and places a new sell order immediately if coin's price drops below chosen stop-loss value.
* Stop-loss is market-proof. This allows stop-loss to be executed only, if the coin's last price was below stop-loss value and above given % of stop-loss value. Useful in case coin's value drops dramatically.

3. Email notifications:
* Notifies if state of the open orders changes.

4. User friendly interface:
* Main View.

![picture](https://github.com/BlackOfWhite/AltCoin/blob/master/images/Main.png)

* Transaction Creator.

![picture](https://github.com/BlackOfWhite/AltCoin/blob/master/images/ClassicTransactionCreator.png)

# Steps to Setup the AltCoin:

1. Go to Bittrex Settings Tab, You need to generate API key to Place BUY or SELL order via Bot. Under API Keys in sidebar:
* Click on Add New Key
* Make *Read Info*, *Trade Limit*, *Trade Market* - ON. **Remember not to give Withdraw permission to your bot**
* Put you 2-Factor Authentication Code
* Click Update Keys
* Now, you will get **KEY** and **SECRET**, Copy them and Store it at safe place as **SECRET** will vanish once page refreshes.

![Screen Shot 2017-07-17 at 1.30.21 PM.png](https://steemitimages.com/DQmTb8v4ygvqdai46CWuFNVUsDQ3ye4MrBVfd6qzxwVPArH/Screen%20Shot%202017-07-17%20at%201.30.21%20PM.png)

2. Download **AltCoin**:

* Git (optional if you don't want to contribute, just download  the repo from github).
* Go To my Github Repo, Download/Clone It: [AltCoin](https://github.com/BlackOfWhite/AltCoin.git)
   `git clone https://github.com/BlackOfWhite/AltCoin.git`
* Go to the location containing **AltCoin.jar** file. You can remove all files except this one.
* Double click **AltCoin.jar** file.

3. Java 8 setup:
* This application requires JDK/JRE in minimum version 1.8.0_144.
* Official release can be downloaded from here: http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html
* You need to setup you JDK/JRE propely, this information can be found here: https://docs.oracle.com/goldengate/1212/gg-winux/GDRAD/java.htm#BGBFJHAB
* Double click **AltCoin.jar** or execute **java -jar AltCoin.jar** from your CLI to start the application.
* Have fun!

4. **AltCoin** setup and configuration


  ```
  API_KEY = "<YOUR_API_KEY>"
  API_SECRET = "<YOUR_API_SECRET>"
  ```

5. Usage:

6. Tips:
* You can check all **AltCoin** events under /logs direcotry. Just pick the one wit the latest timestamp.

7. Test version:
* There is currently only one version of **AltCoin**, therefore it may still contain some bugs. Feel free to mail me at: **niewinskipiotr1993@gmail.com** in case you found any. 
* However, even if this is just a test version / first release, it is **not possible** that your coins would be lost! Feel free to play around!
* Any ideas of the features that should be included in the next release are welcome!


// OLD
* Change *percent_decrease* as per the need : 

`sell_at_any_cost(0.3) if BOT_TYPE == 4` means you want to cancel all open orders and place one sell order at **30%** decrease of the last traded price of the market.

> **BUY_ALL BOT** has same parameters as that of **BUY BOT**.

> **SELL_ALL BOT** has one parameter: 
```
# method to sell all BTC pair orders on bittrex
# params- profit_rate(float)[default = 0.2] at which sell orders need to be set
def sell_all_bot(profit_rate = 0.2)
```
* Change *profit_rate* as per the need : 

`sell_all_bot(0.2) if BOT_TYPE == 6` means you want to place sell orders with **20%** profit on the net purchased value

> **CANCEL_ALL BOT** has no parameters as its task is only to cancel all open orders.
// AND OLD

# Future Scope

* Gain/loss hisotry monitor & linear chart.
* **AltCoin** mobile version (Andorid).
* Adding various automated (bot) modes.
* Adding macros.

**DON'T FORGET TO MAKE DONATIONS IF YOU FIND IT HELPFUL OR MAKE PROFITS OUT OF IT:**

```
Bitcoin(BTC) Address : 168zdkYPAqMjwjior2GRyfbo1djBJL9LTH
Ethereum(ETH) Address : 0xE69Cb31ddD790F072E79aC789f0152dF85b16AF4
Litcoin(LTC) Address : LPBDpTsHd9n6e8ZG4xhUoyUx91CdNmXZWW
```
