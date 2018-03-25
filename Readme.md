# AltBot desktop application for Bittrex Exchange (Wallet/Bot/Stop-loss)

# AltBot features
1. Real time wallet to display all owned coins:
* Shows percentage for every coin.
* Shows BTC equivalent.
* Shows total value of wallet.
![picture](https://github.com/BlackOfWhite/AltBot/blob/master/images/WalletPieChart1.png)

2. Place Buy/Sell order with **stop-loss** option:
* Place normal Bittrex buy/sell orders using this application.
* You can add stop-loss option to every buy/sell order created within AltBot application.
* Stop-loss cancels the order and places a new sell order immediately if coin's price drops below chosen stop-loss value.
* Stop-loss is market-proof. This allows stop-loss to be executed only, if the coin's last price was below stop-loss value and above given % of stop-loss value. Useful in case coin's value drops dramatically.

3. Email notifications:
* Notifies if state of the open orders changes.

4. User friendly interface:
* Main View.

![picture](https://github.com/BlackOfWhite/AltBot/blob/master/images/Main.png)

* Transaction Creator.

![picture](https://github.com/BlackOfWhite/AltBot/blob/master/images/ClassicTransactionCreator.png)



# Steps to Setup the AltBot:
1. Go to Bittrex Settings Tab, You need to generate API key to Place BUY or SELL order via Bot. Under API Keys in sidebar:
* Click on Add New Key
* Make *Read Info*, *Trade Limit*, *Trade Market* - ON. **Remember not to give Withdraw permission to your bot**
* Put your 2-Factor Authentication Code and click Update Keys.
* Now, you will get **KEY** and **SECRET**. Copy them and Store it at safe place as **SECRET** will vanish once page refreshes.

![Screen Shot 2017-07-17 at 1.30.21 PM.png](https://steemitimages.com/DQmTb8v4ygvqdai46CWuFNVUsDQ3ye4MrBVfd6qzxwVPArH/Screen%20Shot%202017-07-17%20at%201.30.21%20PM.png)


2. Download **AltBot**:
* Git (optional if you don't want to contribute, just download  the repo from github).
* Go To my Github Repo, Download/Clone It: [AltBot](https://github.com/BlackOfWhite/AltBot.git)
   `git clone https://github.com/BlackOfWhite/AltBot.git`
* Go to the location containing **AltBot.jar** file. You can remove all files except this one.
* Double click **AltBot.jar** file to run application.

3. Java 8 setup:
* This application requires JDK/JRE in minimum version 1.8.0_144.
* Official release can be downloaded from here: http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html
* You need to setup you JDK/JRE propely, this information can be found here: https://docs.oracle.com/goldengate/1212/gg-winux/GDRAD/java.htm#BGBFJHAB
* Double click **AltBot.jar** or execute **java -jar AltBot.jar** from your CLI to start the application.
* Have fun!

4. **AltBot** setup and configuration
* Navigate to Setting menu and then to **API Setup** to setup your API keys. Enter API key first, then your secret. You will do this only once.
* To setup email notifications you need to enter your email address and a password, just once. It may be a good idea to create new email address just for this purpose, or do not use your main email address. Just for your calmness.

5. Usage:

6. Tips:
* You can check all **AltBot** events under /logs direcotry. Just pick the one wit the latest timestamp.

7. Test version:
* There is currently only one version of **AltBot**, therefore it may still contain some bugs. Feel free to mail me at: **niewinskipiotr1993@gmail.com** in case you found any. 
* However, even if this is just a test version / first release, it is **not possible** that your coins would be lost! Feel free to play around!
* Any ideas of the features that should be included in the next release are welcome!



# Bots

* AVG-PRICE bot:




# Future Scope

* Gain/loss hisotry monitor & linear chart.
* **AltBot** mobile version (Andorid).
* Adding various automated (bot) modes.
* Adding macros.

**DON'T FORGET TO MAKE DONATIONS IF YOU FIND IT HELPFUL OR MAKE PROFITS OUT OF IT:**

```
Bitcoin(BTC) Address : 168zdkYPAqMjwjior2GRyfbo1djBJL9LTH
Ethereum(ETH) Address : 0xE69Cb31ddD790F072E79aC789f0152dF85b16AF4
Litcoin(LTC) Address : LPBDpTsHd9n6e8ZG4xhUoyUx91CdNmXZWW
```
