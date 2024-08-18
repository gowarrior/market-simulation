;********************************************************************************************************************************************************************************
;Global Variables
;********************************************************************************************************************************************************************************
; price - the current price of the asset
; buyQueue - The Bid Side of the Open Order Book
; sellQueue - The Ask Side of the Open Order Book
; tradeWipeQueue - coding wipe queue for transactions
; tradeMatch - trade match id variable
; currentAsk - current best ask price
; currentBid - current best bid price

; numberBid, numberAsk - ploting variables 
; volume - single agent volume of executed trades
; volatility - volatility of the market
; currentPriceVol - used to determine the price volatility
; totalVolume - total volume of executed trades in the market
; currentVol - most recent volume executed

; movingAverage - the moving average price of the asset for 1 min (300 ticks)
; currentMA - the most recent moving average price
; stopMarketMakers - market maker stop counter for markeet crash
; movingAverageV - the moving average volume of the asset for 1 min (300 ticks)
; currentMAV - the most recent moving average volume
; currentBQD - the current bid book depth

; currentSQD - the current ask book depth
; pastMA - previous moving average price
; stablePrice - moving average stable price for market 
; stopTriggered - market stop lose trigger mechanisum 
; stopTriggerCounter - market stop lose trigger mechanisum tick counter
; priceReturns - list of all price returns 

; transactionCost - cost of trading
; aggresiveBid - number of market orders to buy
; aggresiveAsk - number of market orders to sell
; minPriceValue - minimum price of asset, used for measuring flash crash effect
; randNumOT - variable used to represent directional trend in the market which makes trader prefer a side of the order book to participate in
; pastPrices - keeps track of previous prices
; period -allows to keep track of a sequence in a trade strategy

; tradeWipeQueueB, tradeWipeQueueA - list contrining all the trades that occured in a tick period, used for updating the orderbok 
; arrayQ - array contrainging the quantities for each order
; tradeExNumber - used to keep track of the order of actions in the audit trail book
; traderListNumbers - used in verfying theat each trader has a unique trader account number

; priceSpread currentBuyInterest currentSellInterest currentBuyInterestPrice currentSellInterestPrice maxBuyInterest PHIndicatorValue - variables associated with calcuating PH Indicator

; VPIN VPINBins volumeBid volumeAsk VPINCount VPINBinSize VPINFinal VPINValue VPINSeries - variables associated with calcuating VPIN Indicator

; WindowLIndicatorValue WindowLIndicator windowS1 windowS2 windowS3 windowS4 windowS5 windowSeries windowSeries2 - variables associated with calcuating Window Indicator

; advSpdS1 advSpdQ1 advSpdS2 advSpdQ2 advSpdS3 advSpdQ3 advSpdS4 advSpdQ4 advSpdS5 advSpdQ5 advSpread advSpreadSeries advSpreadValue - variables associated with calcuating Adverse Selection Indicator

; priceVolatilityVar - volatility variable, in order to create trends in volatility
; runningMatchId - the running count of match trades
; humanAction - variable controling human trade action

extensions [array]


Globals [price buyQueue sellQueue orderNumber tradeWipeQueue tradeMatch currentAsk currentBid 
        numberBid numberAsk volume volatility  currentPriceVol totalVolume currentVol 
        movingAverage currentMA stopMarketMakers movingAverageV currentMAV currentBQD 
        currentSQD pastMA stablePrice stopTriggered stopTriggerCounter priceReturns 
        transactionCost aggresiveBid aggresiveAsk minPriceValue randNumOT pastPrices period
        tradeWipeQueueB tradeWipeQueueA arrayQ tradeExNumber traderListNumbers
        priceSpread  currentBuyInterest currentSellInterest currentBuyInterestPrice currentSellInterestPrice maxBuyInterest PHIndicatorValue 
        VPIN VPINBins volumeBid volumeAsk VPINCount VPINBinSize VPINFinal VPINValue VPINSeries
        WindowLIndicatorValue WindowLIndicator windowS1 windowS2 windowS3 windowS4 windowS5 windowSeries windowSeries2
        advSpdS1 advSpdQ1 advSpdS2 advSpdQ2 advSpdS3 advSpdQ3 advSpdS4 advSpdQ4 advSpdS5 advSpdQ5 advSpread advSpreadSeries advSpreadValue
        priceVolatilityVar runningMatchId testRoy
        smallTradeLength fundMarketTradeLength marketMakerTradeLength opportunisticTradeLength highTradeLength ClearDumpTrader MarketPause]

;********************************************************************************************************************************************************************************
;Agents Variables
;********************************************************************************************************************************************************************************
; movingAverageDIS - the moving average differance between the moviing average price and the current price, used as a threshold for a trader
; totalCanceled - total number of trades canceled or modified by a trader
; reversion - price reversion mechanisum used to remove positions from market makers and high frequency traders
; typeOfTrader - indicates the type of trader a patch is playing as
; traderNumber - The randomly assigned number to indicate trader in the Order Book

; tradeStatus - Current trade Status (Buy, Sell, Bought, Sold, Canceled) in the Open Order Book
; tradeQuantity - Current Quantity trading in the Open Order Book
; tradeOrderNumber - Number of the Order that is currently in the Open Order Book
; speed - Frequency of the Trader to Trade
; tradeSpeedAdjustment - random variability given to each traders trading speed, baset on class of trader
; tradePrice - Price of the current trade in the Open Order Book
; tradeQ - Current Quantity trading in the Open Order Book, needed for audit trail

; tradeOrderForm - Order Book info
; countticks - the length that trade has been in the book
; sharesOwned - number of shares out standing
; tradeAccount - the porfiatability of that trader
; totalBought - total number shares of bought
; totalSold - total number shares sold

; averageBoughtPrice - average buy price
; averageSoldPrice - average sell price
; marketDumpTimer - variable to impliment stop lose orders
; marketPauseTickCount - market pause mechanisum for agents to stop exicuting tranactions for a select quanitty of time (150 ticks) while the market is pause from a quick price drop
 
; checkTraderNumber - used in verfying theat each trader has a unique trader account number
; buysell - used in the audit trail to determine if the trader is acting on the Buy (B) or Sell (S) side of the order book
; modify - used to determine if an order has been modified 
; matchId - used in matching trades
; AuditTrail - used to turn off the audit trail
; removeTrade - variable used to make sure a previous order is canceled before a new one is placed.
 
turtles-own [ movingAverageDIS totalCanceled reversion typeOfTrader traderNumber 
              tradeStatus tradeQuantity tradeOrderNumber speed tradeSpeedAdjustment tradePrice tradeQ2
              tradeOrderForm countticks sharesOwned tradeAccount totalBought totalSold 
              averageBoughtPrice averageSoldPrice marketDumpTimer marketPauseTickCount
              checkTraderNumber buysell modify matchId AuditTrail removeTrade         
              ]

breed [players player]

players-own [ user-name humanAction sellpriceticks buypriceticks buysize sellsize]

;********************************************************************************************************************************************************************************
;Setup Model Function
;*******************************************************************************************************************************************************************************


to startup
  ;; standard HubNet setup
  hubnet-set-client-interface "COMPUTER" []
  hubnet-reset
  setup-clear
end


to setup-clear
  ca
  setup
end


to setup
  ca
  setup-economy
  file-close-all
  set stopTriggerCounter 0
  set stopTriggered  0
  set stablePrice 1350
  set currentMA 1270
  set volatility [1 2 3 5 7]
  set pastPrices [1 2 3 5 7]
  set movingAverage []
  set movingAverageV []
  set priceReturns []
  set priceSpread []
  set currentPriceVol 1
  set stopMarketMakers False
  set currentBQD 2
  set currentSQD 2
  set transactionCost 0
  set pastMA 1350
  setup-plots
  set arrayQ array:from-list n-values 10000 ["B"]
  
  set randNumOT 0
  set period 0
  set tradeExNumber 0
  set PHIndicatorValue 0
  set VPIN 0
  set WindowLIndicator 0
  set advSpread 0
  set volumeBid 0
  set volumeAsk 0
  set VPINBins [ 0 ]
  set VPINCount 0
  set VPINBinSize 0
  set VPINFinal 0
  set VPINSeries [0 0 0 0 0 0 0 0 0 0]
  set windowS1 [0]
  set windowS2 [0]
  set windowS3 [0]
  set windowS4 [0]
  set windowS5 [0]
  set windowSeries [0]
  set windowSeries2 [0]
  set advSpdS1 [0]
  set advSpdS2 [0]
  set advSpdS3 [0]
  set advSpdS4 [0]
  set advSpdS5 [0]
  set advSpdQ1 [0]
  set advSpdQ2 [0]
  set advSpdQ3 [0]
  set advSpdQ4 [0]
  set advSpdQ5 [0]
  set advSpreadSeries [0]
  set numberAsk []
  set numberBid []
  
  set smallTradeLength 10000
  set fundMarketTradeLength 600
  set marketMakerTradeLength 300
  set opportunisticTradeLength 1200
  set highTradeLength 3
  set ClearDumpTrader False
  set MarketPause False
  ask players [ setup-humantraders ]
end

;********************************************************************************************************************************************************************************
;Initial Model and Agent Conditions
;*******************************************************************************************************************************************************************************

to setup-economy  ;; turtles procedure for setup
  set traderListNumbers [0]
  setup-fundimental_buyers
  setup-fundimental_sellers
  setup-marketmakers
  setup-smalltraders
  setup-opportunistictraders 
  set buyQueue []
  set sellQueue []
  set tradeWipeQueue []
  set tradeWipeQueueB []
  set tradeWipeQueueA []
  setup-highfrequency
end

to setup-fundimental_buyers  ;; turtles procedure for setup
  create-turtles #_Fundamental_Buyers [
  set typeOfTrader "FundamentalBuyers"
  set speed (random-poisson 400)
  set tradeSpeedAdjustment random 300
  set tradeStatus "Neutral"
  set price 1350
  set currentBid (price - 1)
  set currentAsk (price + 1)
  set orderNumber 0
  set countticks 0
  set tradeAccount 0
  set sharesOwned 0
  set totalBought 0
  set totalSold 0
  set averageBoughtPrice 0
  set averageSoldPrice 0
  set marketDumpTimer 0
  set marketPauseTickCount 0
  set totalCanceled 0
  set buysell "B"
  set modify 0
  set matchId [0 0 0 0 0]
  set tradeQ2 0
  set removeTrade 0
  ]
end


to setup-fundimental_sellers  ;; turtles procedure for setup
  create-turtles (#_Fundamental_Sellers )  [
  set typeOfTrader "FundamentalSellers"
  set speed (random-poisson 400)
  set tradeSpeedAdjustment random 300
  set tradeStatus "Neutral"
  set price 1350
  set orderNumber 0
  set countticks 0
  set tradeAccount 0
  set sharesOwned 0
  set totalBought 0
  set totalSold 0
  set averageBoughtPrice 0
  set averageSoldPrice 0
  set marketPauseTickCount 0
  set totalCanceled 0
  set buysell "B"
  set modify 0
  set matchId [0 0 0 0 0]
  set tradeQ2 0
  set removeTrade 0
  ]
end

to setup-marketmakers  ;; turtles procedure for setup
  create-turtles #_Market_Makers [ 
  set typeOfTrader "MarketMakers"
  set speed (random-poisson 30)
  set tradeSpeedAdjustment random 10
  set tradeStatus "Neutral"
  set price 1350
  set orderNumber 0
  set countticks 0
  set tradeAccount 0
  set sharesOwned 0
  set totalBought 0
  set totalSold 0
  set averageBoughtPrice 0
  set averageSoldPrice 0
  set movingAverageDIS 0
  set reversion 0
  set marketPauseTickCount 0
  set totalCanceled 0
  set buysell "B"
  set modify 0
  set matchId [0 0 0 0 0]
  set tradeQ2 0
  set removeTrade 0
  ]
end

to setup-highfrequency  ;; turtles procedure for setup
  create-turtles #_High_Frequency_Traders [ 
  set typeOfTrader "HighFrequency" 
  set speed (1 + (random-poisson 3))
  set tradeSpeedAdjustment random 2
  set tradeStatus "Neutral"
  set price 1350
  set orderNumber 0
  set countticks 0
  set tradeAccount 0
  set sharesOwned 0
  set totalBought 0
  set totalSold 0
  set averageBoughtPrice 0
  set averageSoldPrice 0
  set movingAverageDIS 0
  set reversion 0
  set totalCanceled 0
  set buysell "B"
  set modify 0
  set matchId [0 0 0 0 0]
  set tradeQ2 0
  set removeTrade 0
  ]
end


to setup-smalltraders  ;; turtles procedure for setup
  create-turtles #_Small_Traders [ 
  set typeOfTrader "SmallTraders"
  set speed (random-poisson 15000)
  set tradeSpeedAdjustment random 5000
  set tradeStatus "Neutral"
  set price 1350
  set orderNumber 0
  set countticks 0
  set tradeAccount 0
  set sharesOwned 0
  set totalBought 0
  set totalSold 0
  set averageBoughtPrice 0
  set averageSoldPrice 0
  set marketPauseTickCount 0
  set totalCanceled 0
  set buysell "B"
  set modify 0
  set matchId [0 0 0 0 0]
  set tradeQ2 0
  set removeTrade 0
  ] 
end

to setup-opportunistictraders  ;; turtles procedure for setup
  create-turtles #_Opportunistic_Traders [ 
  set typeOfTrader "OpportunisticTraders"
  set speed (random-poisson 300)
  set tradeSpeedAdjustment random 200
  set tradeStatus "Neutral"
  set price 1350
  set orderNumber 0
  set countticks 0
  set tradeAccount 0
  set sharesOwned 0
  set totalBought 0
  set totalSold 0
  set averageBoughtPrice 0
  set averageSoldPrice 0
  set color white
  set marketDumpTimer 0
  set marketPauseTickCount 0
  set totalCanceled 0
  set buysell "B"
  set modify 0
  set matchId [0 0 0 0 0]
  set tradeQ2 0
  set removeTrade 0
  ] 
end

;********************************************************************************************************************************************************************************
;Function that is run to for every tick of the model to advance it one time step ahead
;********************************************************************************************************************************************************************************

to go
  listen-clients
  eat-bugs
  set currentBuyInterest 0
  set currentSellInterest 0
  set currentBuyInterestPrice 0
  set currentSellInterestPrice 0
  ask turtles [
   if(ticks = 0) [  checkTraderNumberUnique
     ifelse (checkTraderNumber = 1) [set traderListNumbers lput traderNumber traderListNumbers][ checkTraderNumberUnique ]
  ]
  ]
   
   if((ticks mod 10) = 9)[
     set volume 0
     set VPINBinSize ((volumeBid + volumeAsk) + VPINBinSize)
     if((VPINBinSize / 157) > VPINCount)[
       set VPINCount (VPINCount + 1)
       set VPIN (abs(volumeBid - volumeAsk) + VPIN) 
       set VPINBins lput (log ((sum (sublist VPINSeries ((length VPINSeries) - 10) ((length VPINSeries) - 1)) / 1575) + 1) 2) VPINBins
       set VPINSeries lput abs(volumeBid - volumeAsk) VPINSeries 
       if((VPINBinSize / 157) >= 10)[
         set VPINFinal (log ((VPIN / VPINBinSize) + 1) 2) + 0.00001
         ;set VPINBins lput VPINFinal VPINBins
         set VPINCount 0
         set VPIN 0
         set VPINBinSize 0
       ]
       set volumeBid 0
       set volumeAsk 0
     ]
   ]
      
   if( ticks mod 10 = 0)
   [
     set aggresiveBid 0
     set aggresiveAsk 0
   ]
   
   if (ticks mod 200 = 0 and ticks > 9750) [ 
     set randNumOT (randNumOT + int(random-normal 0 100))
     if (randNumOT > 400) [set randNumOT -200]
     if (randNumOT < -400) [set randNumOT 200]
   ]  
   
   if ((ticks + 50) mod 100 = 0 and ticks > 9750) [ 
     set priceVolatilityVar (priceVolatilityVar + int(random-normal 0 2))
     if (randNumOT > 4) [set priceVolatilityVar 0]
     if (randNumOT < -4) [set priceVolatilityVar 0]
   ] 
        
   ask turtles [
   
   set countticks (countticks + 1)
     
   if (typeOfTrader = "SmallTraders" ) [
     if countticks >= (smallTradeLength + tradeSpeedAdjustment)  [
      if tradeStatus = "Buy" [
       set buyQueue remove tradeOrderForm buyQueue
      ]
      if tradeStatus = "Sell" [
       set sellQueue remove tradeOrderForm sellQueue    
      ]
      set tradeStatus "Cancel"
      
      
      if(ticks > 10000) [set totalCanceled (totalCanceled + tradeQuantity)]
      
      if(ticks > 15000)[
      if (tradeStatus != "Neutral" and tradeQuantity != 0)[
           writetofile
      ]]
      set countticks 0
      set speed (random-poisson 30000)
    ]
  ]
  
  if (typeOfTrader = "HumanTrader" ) [
    if (removeTrade = 1) [
      
      if(tradeStatus = "Buy")[
        print filter [? = tradeorderform] buyQueue
        set buyQueue remove tradeOrderForm buyQueue
      ]
      if(tradeStatus = "Sell")[
        set sellQueue remove tradeOrderForm sellQueue
           
      ]
      
      set tradeStatus "Cancel"
      set removeTrade 0
      
      if(ticks > 10000) [set totalCanceled (totalCanceled + tradeQuantity)]
      
      if (tradeStatus != "Neutral")[
           writetofile
      ]
      
      set countticks 0
    ]
  ]
  
  if (typeOfTrader = "OpportunisticTraders" ) [
    ifelse(marketPause = False or marketPauseTickCount > 10)[
      if countticks >= (opportunisticTradeLength + tradeSpeedAdjustment)  [
        if tradeStatus = "Buy" [
          set buyQueue remove tradeOrderForm buyQueue
        ]
        
        if tradeStatus = "Sell" [
          set sellQueue remove tradeOrderForm sellQueue   
        ]
        
        set tradeStatus "Cancel"
               
        if(ticks > 10000) [set totalCanceled (totalCanceled + tradeQuantity)]
      
        if (tradeStatus != "Neutral")[
           writetofile
        ]
        
        set countticks 0
        set speed int(random-poisson 400)
      ]
    ]
    [set countticks (opportunisticTradeLength + tradeSpeedAdjustment)
     set speed int(random-poisson 20) + 2]
  ]
  
  if (typeOfTrader = "FundamentalBuyers" or typeOfTrader = "FundamentalSellers") [
    ifelse(marketPause = False or marketPauseTickCount > 10)[
      if countticks >= (fundMarketTradeLength + tradeSpeedAdjustment) and tradeStatus != "Neutral" [
        if tradeStatus = "Buy" [
          set buyQueue remove tradeOrderForm buyQueue
        ]
        
        if tradeStatus = "Sell" [
          set sellQueue remove tradeOrderForm sellQueue    
        ]
        
        set tradeStatus "Cancel"
               
        if(ticks > 10000) [set totalCanceled (totalCanceled + tradeQuantity)]
      
        if (tradeStatus != "Neutral")[
           writetofile
        ]
        
        set countticks 0
        set speed int(random-poisson 400)
      ]
    ]
    [set countticks (fundMarketTradeLength + tradeSpeedAdjustment)
     set speed int(random-poisson 20) + 2]
  ]
  
  if (typeOfTrader = "MarketMakers") [
    if((1000 + (price / 5)) > currentMA - 12)[
      if(marketPauseTickCount > 10)[set countticks 1]
      if countticks >= (marketMakerTradeLength + tradeSpeedAdjustment)[
        if tradeStatus = "Buy" [
          set buyQueue remove tradeOrderForm buyQueue
        ]
        
        if tradeStatus = "Sell" [
          set sellQueue remove tradeOrderForm sellQueue    
        ]
        
        set tradeStatus "Cancel"
                
        if(ticks > 10000) [set totalCanceled (totalCanceled + tradeQuantity)]
      
        if (tradeStatus != "Neutral")[
           writetofile
        ]
        
        set countticks 0
        set speed int(random-poisson 100) + 2
      ]
    ]
  ]
  
  if (typeOfTrader = "HighFrequency") [
   if countticks >= (highTradeLength + tradeSpeedAdjustment)[
     
        if tradeStatus = "Buy" [
           set buyQueue remove tradeOrderForm buyQueue
        ]
        if tradeStatus = "Sell" [
           set sellQueue remove tradeOrderForm sellQueue    
        ]
        set tradeStatus "Cancel"
               
        if(ticks > 10000) [set totalCanceled (totalCanceled + tradeQuantity)]
      
        if (tradeStatus != "Neutral")[
             writetofile
        ]
        set countticks 0
    ]
  ]
  
  set tradeAccount (((sharesOwned * (1000 + (price / 5)) - averageBoughtPrice * totalBought + averageSoldPrice * totalSold)) - (transactionCost * (totalBought + totalSold)))
  
  if (tradeStatus = "Sold") or (tradeStatus = "Neutral") or (tradeStatus = "Bought") or (tradeStatus = "Cancel") or (typeOfTrader = "HumanTrader" and humanAction != 0)[
    if (ticks + tradeSpeedAdjustment) mod speed = 0 [
      set countticks 0
      set modify 0
        if(typeOfTrader = "FundamentalBuyers") [
          fundBuyerStrategy
        ]
        
        if typeOfTrader = "FundamentalSellers" 
        [
          fundSellerStrategy
        ] 
   
        if typeOfTrader = "OpportunisticTraders"
        [
          strategyOpportunistic
        ] 
   
        if typeOfTrader = "MarketMakers"
        [
          strategyMedium
        ] 
          
        if typeOfTrader = "HighFrequency"
        [
          strategyHigh
        ] 
        
        if typeOfTrader = "SmallTraders" 
        [
          strategyLow
        ]
        
        if (typeOfTrader = "HumanTrader")
        [
          strategyhuman
        ]
        
        if(tradeStatus = "Buy")
        [
           set buysell "B"
        ]
        
        if(tradeStatus = "Sell")
        [
           set buysell "S"
        ]
        
        if(tradePrice <= currentBid and tradeStatus = "Buy")[set aggresiveBid (aggresiveBid + tradeQuantity)]  
        if(tradePrice >= currentAsk and tradeStatus = "Sell")[set aggresiveAsk (aggresiveAsk + tradeQuantity)]  
    
      
      let transactionVar 0
      set tradeQ2 tradeQuantity
      
      array:set arrayQ traderNumber tradeQuantity 
      
      if (removeTrade = 0)[
      if (tradeStatus = "Buy")[ 
        set orderNumber (orderNumber + 1)
        set tradeOrderNumber orderNumber
        set tradeOrderForm  (int(tradePrice) * 10000000000000) + (int(orderNumber) * 10000000) + (int(traderNumber) * 100) + tradeQuantity
        set buyQueue lput tradeOrderForm buyQueue
        set countticks 0
                 
        if (tradeStatus != "Neutral")[
          writetofile
        ]
      ]
      
      set transactionVar 1
         
      if (tradeStatus = "Sell")[ 
        set orderNumber (orderNumber + 1)
        set tradeOrderNumber orderNumber
        set tradeOrderForm  (int(tradePrice) * 10000000000000) + (int(orderNumber) * 10000000) + (int(traderNumber) * 100) + tradeQuantity
        set sellQueue lput tradeOrderForm sellQueue
        set countticks 0
            
        if (tradeStatus != "Neutral")[
           writetofile
        ]
      ]
      ]
    

      if (ticks > 10000 ) [
        if (MarketPause = False) [
          set marketPauseTickCount 0
          transaction          
        ]
        if (MarketPause = true) [
          set marketPauseTickCount (marketPauseTickCount + 1)
          if(marketPauseTickCount >= 150)
          [
            set marketPauseTickCount 0
            set MarketPause False
          ]
        ]
      ]
    ]]  
  ]
  
  
  if ( length buyQueue > 0 )
  [
    set numberBid []
    histoSetup1
    setup-plot1
  ]
  
  if ( length sellQueue > 0 )
  [
    set numberAsk []
    histoSetup2
    
  ]
 
  do-plots
  if(ticks > 10000)[
  
  ask turtles [
    if ( length tradeMatch > 0)
    [
      let traderQueueCount 0
      loop  [
        if (item traderQueueCount tradeMatch = traderNumber) [
          ifelse(item 0 matchId = 0)[
            set matchId replace-item 0 matchId round (runningMatchId + (1 + traderQueueCount) / 2)]
          [ifelse(item 1 matchId = 0)[
             set matchId replace-item 1 matchId round (runningMatchId + (1 + traderQueueCount) / 2)]
            [ifelse(item 2 matchId = 0)[
               set matchId replace-item 2 matchId round (runningMatchId + (1 + traderQueueCount) / 2)]
              [ifelse(item 3 matchId = 0)[
                 set matchId replace-item 3 matchId round (runningMatchId + (1 + traderQueueCount) / 2)]
                [if(item 4 matchId = 0)[
                   set matchId replace-item 4 matchId round (runningMatchId + (1 + traderQueueCount) / 2)]          
                ]
              ]
            ]
          ]
        ]
        set traderQueueCount (traderQueueCount + 1)
        if((length tradeMatch) = traderQueueCount) [stop]
      ]
    ]
  ]
  
  if(length tradeMatch > 0)[
  set runningMatchId (runningMatchId - 1 + round((1 + (length tradeMatch)) / 2)) ]  
  
  ask turtles [
    if ( length tradeWipeQueue > 0)
    [
      let traderQueueCount 0
      loop  [
        if (item traderQueueCount tradeWipeQueue = traderNumber) [
          if tradeStatus = "Sell" [
            set sharesOwned (sharesOwned - tradeQuantity)
       
          ]
          if tradeStatus = "Buy" [
            set sharesOwned (sharesOwned + tradeQuantity)
          
          ] 
          
          if tradeStatus = "Buy" [
            set tradeStatus "Bought"
            set volume (volume + tradeQuantity)
            set volumeBid (volumeBid + tradeQuantity)
            set averageBoughtPrice (((averageBoughtPrice * totalBought) + (tradeQuantity * (1000 + (tradePrice / 5)))) / (tradeQuantity + totalBought))
            set totalBought (totalBought + tradeQuantity)
            
            set modify (modify + 1)
          ]
          
          if tradeStatus = "Sell" [
            set tradeStatus "Sold"
            set volume (volume + tradeQuantity)
            set volumeAsk (volumeAsk + tradeQuantity)
            set averageSoldPrice (((averageSoldPrice * totalSold) + (tradeQuantity * (1000 + (tradePrice / 5)))) / (tradeQuantity + totalSold))
            set totalSold (totalSold + tradeQuantity)
            set modify (modify + 1)
          ]
          
          if (tradeStatus != "Neutral")[
           writetofile
         ] 
       ]
       set traderQueueCount (traderQueueCount + 1)
       
       if((length tradeWipeQueue) = traderQueueCount) [stop]
       
      ]
    ]
  ]
  
  ask turtles [
    if ( length tradeWipeQueueB > 0)
    [
      let traderQueueCount 0
      loop  [
        let traderListNumber int ((item traderQueueCount tradeWipeQueueB) / 100)
        let traderListQ int ((item traderQueueCount tradeWipeQueueB) - (traderListNumber * 100))
        if (traderListNumber = traderNumber) [
          set tradeQuantity (tradeQuantity - traderListQ)
          set modify (modify + 1)
                 
          if tradeStatus = "Sell" [
            set sharesOwned (sharesOwned - traderListQ)        
          ]
          if tradeStatus = "Buy" [
            set sharesOwned (sharesOwned + traderListQ)         
          ] 
          
          if tradeStatus = "Buy" [
            set volume (volume + traderListQ)
            ifelse((traderListQ + totalBought) = 0)[
            set averageBoughtPrice (((averageBoughtPrice * totalBought) + (traderListQ * (1000 + (tradePrice / 5)))) / 1)]
            [ set averageBoughtPrice (((averageBoughtPrice * totalBought) + (traderListQ * (1000 + (tradePrice / 5)))) / (traderListQ + totalBought))]
            set totalBought (totalBought + traderListQ)
          ]
          
          if tradeStatus = "Sell" [
            set tradeStatus "Sold"
            set averageSoldPrice (((averageSoldPrice * totalSold) + (traderListQ * (1000 + (tradePrice / 5)))) / (traderListQ + totalSold))
            set totalSold (totalSold + traderListQ)
            set volume (volume + traderListQ)
          ]
          
          if (tradeStatus != "Neutral" and AuditTrail = true)[
            set tradeExNumber (tradeExNumber + 1)
            file-open "OrderBook.csv"
            file-type tradeOrderNumber ;ORDERID
            file-type ", "
            file-type word tradeOrderNumber modify;HON1
            file-type ", "
            if(modify > 0)[
              let a (modify - 1)
              file-type word tradeOrderNumber a;HON2
              file-type ", "
            ]
            if(modify = 0)[
              file-type "-";HON2
              file-type ", "
            ]
            file-type traderNumber ;FIRM
            file-type ", "
            file-type buysell ;BUYSELL
            file-type ", "
            file-type "105" ;FUNCCODE
            file-type ", "
            file-type ((tradePrice / 5 + 1000)) ;PRICE
            file-type ", "
            file-type traderListQ ;QUANTITY
            file-type ", "
            file-type hour ;TIME
            file-type ":" 
            file-type minute 
            file-type ":"
            file-type second
            file-type ", "
            file-type hour ;CONFTIME
            file-type ":" 
            file-type minute 
            file-type ":"
            file-type second
            file-type ", "
            if(typeOfTrader = "OpportunisticTraders")
            [
              file-type "1" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "FundamentalSellers")
            [
              file-type "2" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "FundamentalBuyers")
            [
              file-type "3" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "HighFrequency")
            [
              file-type "4" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "MarketMakers")
            [
              file-type "5" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "IRLTrader")
            [
              file-type "9" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "DumpTrader")
            [
              file-type "7" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "SmallTraders")
            [
              file-type "6" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "IcebergTrader")
            [
              file-type "8" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "HumanTrader")
            [
              file-type "10" ;CTI
              file-type ", "
            ]
            file-type ((currentAsk / 5 + 1000)) ;ASKPRICE
            file-type ", "
            file-type occurrences (((currentAsk / 5 + 1000))* 10) numberAsk ;ASKQUANTITY
            file-type ", "
            file-type ((currentBid / 5 + 1000)) ;BIDPRICE
            file-type ", "
            file-type occurrences (((currentBid / 5 + 1000))* 10) numberBid ;BIDQUANTITY
            file-type ", "
            file-type "CME" ;EXCHANGE
            file-type ", "
            file-type "-" ;MAX SHOW FLAG 
            file-type ", "
            ifelse(item 0 matchId = 0 and item 1 matchId = 0 and item 2 matchId = 0 and item 3 matchId = 0 and item 4 matchId = 0)
            [
              file-type "-" ;MATCH NUMBER
              file-type ", "
            ]
            [
              ifelse(item 0 matchId > 0)[ 
                file-type item 0 matchId ;MATCH NUMBER
                file-type ", "
                set matchId replace-item 0 matchId 0
              ]
              [
                ifelse(item 1 matchId > 0)[ 
                  file-type item 1 matchId ;MATCH NUMBER
                  file-type ", "
                  set matchId replace-item 0 matchId 0
                ]
                [
                  ifelse(item 2 matchId > 0)[ 
                    file-type item 2 matchId ;MATCH NUMBER
                    file-type ", "
                    set matchId replace-item 2 matchId 0
                  ]
                  [
                    ifelse(item 3 matchId > 0)[ 
                      file-type item 3 matchId ;MATCH NUMBER
                      file-type ", "
                      set matchId replace-item 3 matchId 0
                    ]
                    [
                      file-type item 4 matchId ;MATCH NUMBER
                      file-type ", "
                      set matchId replace-item 4 matchId 0
                    ]
                  ]
                ]
              ]
            ]
            file-type tradeExNumber ;TRANSACTION ID
            file-print ", "
            file-close
                        
            set modify (modify + 1)
            
            set tradeExNumber (tradeExNumber + 1)
            let q array:item arrayQ traderNumber
            file-open "OrderBook.csv"
            file-type tradeOrderNumber ;ORDERID
            file-type ", "
            file-type word tradeOrderNumber modify;HON1
            file-type ", "
            if(modify > 0)[
              let a (modify - 1)
              file-type word tradeOrderNumber a;HON2
              file-type ", "
            ]
            if(modify = 0)[
              file-type "-";HON2
              file-type ", "
            ]
            file-type traderNumber ;FIRM
            file-type ", "
            file-type buysell ;BUYSELL
            file-type ", "
            file-type "605" ;FUNCCODE
            file-type ", "
            file-type ((tradePrice / 5 + 1000)) ;PRICE
            file-type ", "
            set tradeQ2 (tradeQ2 - traderListQ)
            file-type tradeQ2  ;QUANTITY
            file-type ", "
            file-type hour ;TIME
            file-type ":" 
            file-type minute 
            file-type ":"
            file-type second
            file-type ", "
            file-type hour ;CONFTIME
            file-type ":" 
            file-type minute 
            file-type ":"
            file-type second
            file-type ", "
            if(typeOfTrader = "OpportunisticTraders")
            [
              file-type "1" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "FundamentalSellers")
            [
              file-type "2" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "FundamentalBuyers")
            [
              file-type "3" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "HighFrequency")
            [
              file-type "4" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "MarketMakers")
            [
              file-type "5" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "IRLTrader")
            [
              file-type "9" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "DumpTrader")
            [
              file-type "7" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "SmallTraders")
            [
              file-type "6" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "IcebergTrader")
            [
              file-type "8" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "HumanTrader")
            [
              file-type "10" ;CTI
              file-type ", "
            ]
            file-type ((currentAsk / 5 + 1000)) ;ASKPRICE
            file-type ", "
            file-type occurrences (((currentAsk / 5 + 1000))* 10) numberAsk ;ASKQUANTITY
            file-type ", "
            file-type ((currentBid / 5 + 1000)) ;BIDPRICE
            file-type ", "
            file-type occurrences (((currentBid / 5 + 1000))* 10) numberBid ;BIDQUANTITY
            file-type ", "
            file-type "CME" ;EXCHANGE
            file-type ", "
            file-type "-" ;MAX SHOW FLAG 
            file-type ", "
            file-type "-" ;MATCH NUMBER
            file-type ", "
            file-type tradeExNumber ;TRANSACTION ID
            file-print ", "
            file-close
          ]
       ]
       set traderQueueCount (traderQueueCount + 1)
       
       if((length tradeWipeQueueB) = traderQueueCount) [stop]
       
      ]
    ]
  ]
  
  ask turtles [
    if ( length tradeWipeQueueA > 0)
    [
      let traderQueueCount 0
      loop  [
        let traderListNumber int ((item traderQueueCount tradeWipeQueueA) / 100)
        let traderListQ int ((item traderQueueCount tradeWipeQueueA) - (traderListNumber * 100))
        if (traderListNumber = traderNumber) [
          set tradeQuantity (tradeQuantity - traderListQ)
          set modify (modify + 1)       
                 
          if tradeStatus = "Sell" [
            set sharesOwned (sharesOwned - traderListQ)
            
            
          ]
          if tradeStatus = "Buy" [
            set sharesOwned (sharesOwned + traderListQ)
           
          ] 
          
          if tradeStatus = "Buy" [
            set tradeStatus "Bought"
            set volume (volume + traderListQ)
            set averageBoughtPrice (((averageBoughtPrice * totalBought) + (traderListQ * (1000 + (tradePrice / 5)))) / (traderListQ + totalBought))
            set totalBought (totalBought + traderListQ)
          ]
          
          if tradeStatus = "Sell" [
            set volume (volume + traderListQ)
            set averageSoldPrice (((averageSoldPrice * totalSold) + (traderListQ * (1000 + (tradePrice / 5)))) / (traderListQ + totalSold))
            set totalSold (totalSold + traderListQ)
          ]

          if (tradeStatus != "Neutral" and AuditTrail = true)[
            set tradeExNumber (tradeExNumber + 1)
            file-open "OrderBook.csv"
            file-type tradeOrderNumber ;ORDERID
            file-type ", "
            file-type word tradeOrderNumber modify;HON1
            file-type ", "
            if(modify > 0)[
              let a (modify - 1)
              file-type word tradeOrderNumber a;HON2
              file-type ", "
            ]
            if(modify = 0)[
              file-type "-";HON2
              file-type ", "
            ]
            file-type traderNumber ;FIRM
            file-type ", "
            file-type buysell ;BUYSELL
            file-type ", "
            file-type "105" ;FUNCCODE
            file-type ", "
            file-type ((tradePrice / 5 + 1000)) ;PRICE
            file-type ", "
            file-type traderListQ ;QUANTITY
            file-type ", "
            file-type hour ;TIME
            file-type ":" 
            file-type minute 
            file-type ":"
            file-type second
            file-type ", "
            file-type hour ;CONFTIME
            file-type ":" 
            file-type minute 
            file-type ":"
            file-type second
            file-type ", "
            if(typeOfTrader = "OpportunisticTraders")
            [
              file-type "1" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "FundamentalSellers")
            [
              file-type "2" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "FundamentalBuyers")
            [
              file-type "3" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "HighFrequency")
            [
              file-type "4" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "MarketMakers")
            [
              file-type "5" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "IRLTrader")
            [
              file-type "9" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "DumpTrader")
            [
              file-type "7" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "SmallTraders")
            [
              file-type "6" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "IcebergTrader")
            [
              file-type "8" ;CTI
              file-type ", "
            ]
            file-type ((currentAsk / 5 + 1000)) ;ASKPRICE
            file-type ", "
            file-type occurrences (((currentAsk / 5 + 1000))* 10) numberAsk ;ASKQUANTITY
            file-type ", "
            file-type ((currentBid / 5 + 1000)) ;BIDPRICE
            file-type ", "
            file-type occurrences (((currentBid / 5 + 1000))* 10) numberBid ;BIDQUANTITY
            file-type ", "
            file-type "CME" ;EXCHANGE
            file-type ", "
            file-type "-" ;MAX SHOW FLAG 
            file-type ", "
            ifelse(item 0 matchId = 0 and item 1 matchId = 0 and item 2 matchId = 0 and item 3 matchId = 0 and item 4 matchId = 0)
            [
              file-type "-" ;MATCH NUMBER
              file-type ", "
            ]
            [
              ifelse(item 0 matchId > 0)[ 
                file-type item 0 matchId ;MATCH NUMBER
                file-type ", "
                set matchId replace-item 0 matchId 0
              ]
              [
                ifelse(item 1 matchId > 0)[ 
                  file-type item 1 matchId ;MATCH NUMBER
                  file-type ", "
                  set matchId replace-item 0 matchId 0
                ]
                [
                  ifelse(item 2 matchId > 0)[ 
                    file-type item 2 matchId ;MATCH NUMBER
                    file-type ", "
                    set matchId replace-item 2 matchId 0
                  ]
                  [
                    ifelse(item 3 matchId > 0)[ 
                      file-type item 3 matchId ;MATCH NUMBER
                      file-type ", "
                      set matchId replace-item 3 matchId 0
                    ]
                    [
                      file-type item 4 matchId ;MATCH NUMBER
                      file-type ", "
                      set matchId replace-item 4 matchId 0
                    ]
                  ]
                ]
              ]
            ]
            file-type tradeExNumber ;TRANSACTION ID
            file-print ", "
            file-close
            
            set modify (modify + 1)
            
            set tradeExNumber (tradeExNumber + 1)
            let q array:item arrayQ traderNumber
            file-open "OrderBook.csv"
            file-type tradeOrderNumber ;ORDERID
            file-type ", "
            file-type word tradeOrderNumber modify;HON1
            file-type ", "
            if(modify > 0)[
              let a (modify - 1)
              file-type word tradeOrderNumber a;HON2
              file-type ", "
            ]
            if(modify = 0)[
              file-type "-";HON2
              file-type ", "
            ]
            file-type traderNumber ;FIRM
            file-type ", "
            file-type buysell ;BUYSELL
            file-type ", "
            file-type "605" ;FUNCCODE
            file-type ", "
            file-type ((tradePrice / 5 + 1000)) ;PRICE
            file-type ", "
            set tradeQ2 (tradeQ2 - traderListQ)
            file-type tradeQ2  ;QUANTITY ;QUANTITY
            file-type ", "
            file-type hour ;TIME
            file-type ":" 
            file-type minute 
            file-type ":"
            file-type second
            file-type ", "
            file-type hour ;CONFTIME
            file-type ":" 
            file-type minute 
            file-type ":"
            file-type second
            file-type ", "
            if(typeOfTrader = "OpportunisticTraders")
            [
              file-type "1" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "FundamentalSellers")
            [
              file-type "2" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "FundamentalBuyers")
            [
              file-type "3" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "HighFrequency")
            [
              file-type "4" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "MarketMakers")
            [
              file-type "5" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "IRLTrader")
            [
              file-type "9" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "DumpTrader")
            [
              file-type "7" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "SmallTraders")
            [
              file-type "6" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "IcebergTrader")
            [
              file-type "8" ;CTI
              file-type ", "
            ]
            if(typeOfTrader = "HumanTrader")
            [
              file-type "10" ;CTI
              file-type ", "
            ]
            file-type ((currentAsk / 5 + 1000)) ;ASKPRICE
            file-type ", "
            file-type occurrences (((currentAsk / 5 + 1000))* 10) numberAsk ;ASKQUANTITY
            file-type ", "
            file-type ((currentBid / 5 + 1000)) ;BIDPRICE
            file-type ", "
            file-type occurrences (((currentBid / 5 + 1000))* 10) numberBid ;BIDQUANTITY
            file-type ", "
            file-type "CME" ;EXCHANGE
            file-type ", "
            file-type "-" ;MAX SHOW FLAG 
            file-type ", "
            file-type "-" ;MATCH NUMBER
            file-type ", "
            file-type tradeExNumber ;TRANSACTION ID
            file-print ", "
            file-close
          ]
       ]
       set traderQueueCount (traderQueueCount + 1)
       
       if((length tradeWipeQueueA) = traderQueueCount) [stop]
       
      ]
    ]
  ]]
  

  
  let clearQueue []
  set tradeWipeQueue clearQueue
  set tradeWipeQueueA clearQueue
  set tradeWipeQueueB clearQueue
  set tradeMatch clearQueue
  
  if(ticks >= 10000)
  [
    if((ticks - 9700) mod 300 = 0 )
    [
      calculatePriceReturns
    ]
    if((ticks - 9700) mod 10 = 0 )
    [
      calculateSpread
    ]
  ]
    
  if(ticks > 12000) [
    set minPriceValue min movingAverage
  ]
  

  ask turtles [
    set matchId [0 0 0 0 0]
  ]
  
  tick
end

;********************************************************************************************************************************************************************************
;Trader Startegies
;********************************************************************************************************************************************************************************

;Automated Small Trader
;********************************************************************************************************************************************************************************
to strategyLow
  let randNum (random 2)
  let action "Neutral"
  set tradePrice price 
  if (randNum = 1) [
    ifelse (stopTriggered = 0)
    [
      set action "Sell"
      distPriceSmallSell
    ]
    [
      set action "Sell"
      set tradePrice int(stablePrice + random 3)     
    ]
  ]
  if (randNum = 0) [
    ifelse (stopTriggered = 0)
    [
      set action "Buy"
      distPriceSmallBuy
    ]
    [
      set action "Buy"
      set tradePrice int(stablePrice - random 3)    
    ]
  ]
  set tradeStatus action  
  distOrderSmall
end

;Automated Fundamentals Buyers Trader
;********************************************************************************************************************************************************************************
to fundBuyerStrategy
  ifelse (stopTriggered = 0)
  [
    set tradeStatus "Buy"
    distPriceFundamentalBuy
    distOrderFundamental
  ]
  [
    set tradeStatus "Buy"
    set tradePrice int(stablePrice - random 4)
    distOrderFundamental
  ]
end

;Automated Fundamentals Sellers Trader
;********************************************************************************************************************************************************************************
to fundSellerStrategy
  ifelse (stopTriggered = 0)
  [
    set tradeStatus "Sell" 
    distPriceFundamentalSell
    distOrderFundamental
  ]
  [
    set tradeStatus "Sell" 
    set tradePrice int(stablePrice + random 4)
    distOrderFundamental
  ]

end

;Automated Opportunistic Trader
;********************************************************************************************************************************************************************************
to strategyOpportunistic
  let randNum random 1000
  if(ticks < 10002) [ set randNum (100 + random 900) ]
  set tradePrice price
  let action "Neutral" 
  if (randNum > 100) [
    ifelse (stopTriggered = 0)
    [
      set tradeStatus "Buy"
      distPriceOpportunisticBuy
      distOrderOpportunistic
      set tradePrice (tradePrice + priceVolatilityVar)
    ]
    [
      set tradeStatus "Buy"
      set tradePrice int(stablePrice - random 4)
      distOrderOpportunistic
    ]
  ]
  if (randNum > 99 and randNum <= 549 + randNumOT) [
    ifelse (stopTriggered = 0)
    [
      set tradeStatus "Sell" 
      distPriceOpportunisticSell
      distOrderOpportunistic
      set tradePrice (tradePrice + priceVolatilityVar)
    ]
    [
      set tradeStatus "Sell" 
      set tradePrice int(stablePrice + random 4)
      distOrderOpportunistic
    ]
  ]
    
  if (randNum < 100)[
    let numberOfBids (length buyQueue)  ;since we only use length this was needed to make sure there was an accurate count
    let numberOfAsks length sellQueue
    let difference (numberOfBids - numberOfAsks)

    ifelse (sharesOwned < 1 and sharesOwned > -1 and reversion != 1)
    [  
      ifelse(abs(currentMA - (1000 + (price / 5))) > 10 or movingAverageDIS = 1)
      [
        ifelse(abs(currentMA - (1000 + (price / 5))) <= 2)
        [
          set movingAverageDIS  0
        ]
        [
          set movingAverageDIS  1
          ifelse(currentMA > (1000 + (price / 5)))
          [
            set action "Buy"
            set tradePrice (currentBid + 1)
          ]
          [
            set action "Sell"
            set tradePrice (price - 1)
          ]
        ]
      ]
      [
        ifelse( random 100 < 25 and ( sharesOwned > 1 or sharesOwned < -1))
        [
          ifelse (sharesOwned > 0)
          [
            set action "Sell"
            set tradePrice (currentAsk )
          ]
          [
            set action "Buy"
            set tradePrice (currentBid )
          ]
        ]
        [
          ifelse ( random 100 < 50)
          [
            ifelse (numberOfAsks >= numberOfBids) [
              set action "Sell"
              set tradePrice (currentAsk - 2)
            ]
            [
              set action "Buy"
              set tradePrice (currentBid + 2)
            ]
          ]
          [
            ifelse (numberOfAsks >= numberOfBids) [
              set action "Sell"
              set tradePrice (currentAsk )
            ]
            [
              set action "Buy"
              set tradePrice (currentBid)
            ]
          ]
        ]
      ]  
    ]
    [
      ifelse (sharesOwned > 0)
      [
        set action "Sell"
        ifelse (random 100 < 10  or reversion = 1)  
        [
          set reversion 1
          ifelse(sharesOwned > 2)
          [
            set tradePrice (currentAsk - 2)
          ]
          [
            set reversion 0
            set tradePrice (currentAsk - 2)
          ]
        ]
        [ 
          ifelse (random 100 < 90)
          [
            set tradePrice (currentAsk - 2)
          ]
          [
            set tradePrice (currentAsk )
          ]
        ] 
      ]
      [
        set action "Buy"
        ifelse (random 100 < 10  or reversion = 1)  
        [
          set reversion 1
          ifelse(sharesOwned < -2)
          [
            set tradePrice (currentBid + 2)
          ]
          [
            set reversion 0
            set tradePrice (currentBid + 2)
          ]
        ]
        [   
          ifelse (random 100 < 90)
          [
            set tradePrice (currentBid + 2)
          ]
          [  
            set tradePrice (currentBid )
          ]
        ]
      ]
    ]
    set tradeQuantity int(1)
    set tradeStatus action
  ]
  if(randNum < 50)[
  ifelse (sharesOwned < 2 and sharesOwned > -2 and reversion != 1)
  [  
   ifelse(abs(currentMA - (1000 + (price / 5))) > 10 or movingAverageDIS = 1)
   [
     ifelse(abs(currentMA - (1000 + (price / 5))) <= 2)
     [
       set movingAverageDIS  0
     ]
     [
       set movingAverageDIS  1
       ifelse(currentMA > (1000 + (price / 5)))
       [
         set action "Buy"
         set tradePrice (currentBid + 1)
       ]
       [
         set action "Sell"
         set tradePrice (price - 1)
       ]
     ]
   ]
   [
     ifelse( random 100 < 25 and ( sharesOwned > 2 or sharesOwned < -2))
     [
       ifelse (sharesOwned > 0)
       [
         set action "Sell"
         set tradePrice (currentAsk )
       ]
       [
         set action "Buy"
         set tradePrice (currentBid )
       ]
     ]
     [
       ifelse ( random 100 < 50)
       [
          ifelse (currentSQD >= currentBQD and currentSQD <= currentBQD + 15) [
            set action "Sell"
            set tradePrice (currentAsk - 2)
          ]
          [
            set action "Buy"
            set tradePrice (currentBid + 2)
          ]
        ]
        [
          ifelse (currentSQD >= currentBQD and currentSQD <= currentBQD + 15) [
            set action "Sell"
            set tradePrice (currentAsk )
          ]
          [
            set action "Buy"
            set tradePrice (currentBid)
          ]
        ]
      ]
    ]  
  ]
  [
    ifelse (sharesOwned > 0)
    [
      set action "Sell"
      ifelse (random 100 < 10  or reversion = 1)  
      [
        set reversion 1
        ifelse(sharesOwned > 2)
        [
          set tradePrice (currentAsk - 2)
        ]
        [
          set reversion 0
          set tradePrice (currentAsk - 2)
        ]
      ]
      [ 
        ifelse (random 100 < 50)
        [
          set tradePrice (currentAsk - 2)
        ]
        [
          set tradePrice (currentAsk )
        ]
      ] 
    ]
    [
      ifelse( currentSQD >= currentBQD + 30)[
      set action "Buy"
      ifelse (random 100 < 10  or reversion = 1)  
      [
        set reversion 1
        ifelse(sharesOwned < -2)
        [
          set tradePrice (currentBid + 2)
        ]
        [
          set reversion 0
          set tradePrice (currentBid + 2)
        ]
      ]
      [   
        ifelse (random 100 < 50)
        [
          set tradePrice (currentBid + 2)
        ]
        [  
          set tradePrice (currentBid )
        ]
      ]
    ][set tradePrice (currentBid + 2) 
      set action "Sell" ]]
  ]
  if(sharesOwned < -4)[
      set action "Buy"
      set tradePrice (price + random 4 - 2)
      ]
  if(sharesOwned > 4)[
      set action "Sell"
      set tradePrice (price - random 4 + 3)
      ]
  if (ticks < 10002)[
    if (action = "Buy")[set tradePrice price - 1 - random 4]
    if (action = "Sell")[set tradePrice price + 1 + random 4]
  ]
  
  set tradeStatus action
  ]
    
end

;Automated Market Makers
;********************************************************************************************************************************************************************************
to strategyMedium
  let numberOfBids (length buyQueue)  ;since we only use length this was needed to make sure there was an accurate count
  let numberOfAsks length sellQueue
  let difference (numberOfBids - numberOfAsks)
  let action "Neutral"

  ifelse (sharesOwned < 2 and sharesOwned > -2 and reversion != 1)
  [  
   ifelse(abs(currentMA - (1000 + (price / 5))) > 10 or movingAverageDIS = 1)
   [
     ifelse(abs(currentMA - (1000 + (price / 5))) <= 2)
     [
       set movingAverageDIS  0
     ]
     [
       set movingAverageDIS  1
       ifelse(currentMA > (1000 + (price / 5)))
       [
         set action "Buy"
         set tradePrice (currentBid + 1)
       ]
       [
         set action "Sell"
         set tradePrice (price - 1)
       ]
     ]
   ]
   [
     ifelse( random 100 < 25 and ( sharesOwned > 2 or sharesOwned < -2))
     [
       ifelse (sharesOwned > 0)
       [
         set action "Sell"
         set tradePrice (currentAsk )
       ]
       [
         set action "Buy"
         set tradePrice (currentBid )
       ]
     ]
     [
       ifelse ( random 100 < 50)
       [
          ifelse (currentSQD >= currentBQD and currentSQD <= currentBQD + 15) [
            set action "Sell"
            set tradePrice (currentAsk - 2)
          ]
          [
            set action "Buy"
            set tradePrice (currentBid + 2)
          ]
        ]
        [
          ifelse (currentSQD >= currentBQD and currentSQD <= currentBQD + 15) [
            set action "Sell"
            set tradePrice (currentAsk )
          ]
          [
            set action "Buy"
            set tradePrice (currentBid)
          ]
        ]
      ]
    ]  
  ]
  [
    ifelse (sharesOwned > 0)
    [
      set action "Sell"
      ifelse (random 100 < 10  or reversion = 1)  
      [
        set reversion 1
        ifelse(sharesOwned > 2)
        [
          set tradePrice (currentAsk - 2)
        ]
        [
          set reversion 0
          set tradePrice (currentAsk - 2)
        ]
      ]
      [ 
        ifelse (random 100 < 50)
        [
          set tradePrice (currentAsk - 2)
        ]
        [
          set tradePrice (currentAsk )
        ]
      ] 
    ]
    [
      ifelse( currentSQD >= currentBQD + 30)[
      set action "Buy"
      ifelse (random 100 < 10  or reversion = 1)  
      [
        set reversion 1
        ifelse(sharesOwned < -2)
        [
          set tradePrice (currentBid + 2)
        ]
        [
          set reversion 0
          set tradePrice (currentBid + 2)
        ]
      ]
      [   
        ifelse (random 100 < 50)
        [
          set tradePrice (currentBid + 2)
        ]
        [  
          set tradePrice (currentBid )
        ]
      ]
    ][set tradePrice (currentBid + 2) 
      set action "Sell" ]]
  ]
  if(sharesOwned < -4)[
      set action "Buy"
      set tradePrice (price + random 4 - 2)
      ]
  if(sharesOwned > 4)[
      set action "Sell"
      set tradePrice (price - random 4 + 3)
      ]
  if (ticks < 10002)[
    if (action = "Buy")[set tradePrice price - 1 - random 4]
    if (action = "Sell")[set tradePrice price + 1 + random 4]
  ]
  
  set tradeStatus action
  distOrderMarketMaker
end

;Automated High Frequecy Trader
;********************************************************************************************************************************************************************************
to strategyHigh
  let numberOfBids length buyQueue
  let numberOfAsks length sellQueue
  let action "Neutral"
 
  ifelse (sharesOwned < 3  and sharesOwned > -3 and reversion = 0)
  [
    ifelse(abs(currentMA - (1000 + (price / 5))) > 6 or movingAverageDIS = 1)
    [
      ifelse(abs(currentMA - (1000 + (price / 5))) <= 2)
      [
        set movingAverageDIS  0
      ]
      [
        set movingAverageDIS  1
        ifelse(currentMA > (1000 + (price / 5)))
        [
          set action "Buy"
          set tradePrice (currentBid + random 4 - 3 )
        ]
        [
          set action "Sell"
          set tradePrice (price - 1)
        ]
      ]
    ]
    [
      ifelse( random 100 < 30 and ( sharesOwned > 6 or sharesOwned < -6))
      [
        ifelse (sharesOwned > 0)
        [
          set action "Sell"
          set tradePrice (currentAsk - random 4 + 3 )
        ]
        [
          set action "Buy"
          set tradePrice (currentBid + random 4 - 3)
        ]
      ]
      [
        ifelse(currentSQD >= currentBQD + random 21 - 10) 
        [
          set action "Sell"
          set tradePrice (price - random 3 + 1)
        ]
        [
          set action "Buy"
          set tradePrice (price + random 3 - 1)
        ]
      ]
    ]   
  ]
  [
    if (sharesOwned >= 3 or reversion = 1)  
    [
      if(sharesOwned > 0)
      [
        set action "Sell"
        ifelse (random 100 < 60  or reversion = 1)  
        [
          set reversion 1
          ifelse(sharesOwned > 6)
          [
            set tradePrice (price - random 4 + 3)
          ]
          [
            set reversion 0
            set tradePrice (price - random 4 + 3)
          ]
        ]
        [
          ifelse (random 100 < 10)
          [
            set tradePrice (currentAsk - random 4 + 3)
          ]
          [
            set tradePrice (currentAsk)
          ]
        ]
      ]
    ]
    if (sharesOwned <= -3 or reversion = 1)  
    [
      if(sharesOwned < 0)
      [
        set action "Buy"
        ifelse (random 100 < 60  or reversion = 1)  
        [
          set reversion 1
          ifelse(sharesOwned < -6)
          [
            set tradePrice (price + random 4 - 3)
          ]
          [
            set reversion 0
            set tradePrice (price + random 4 - 3)
          ]
        ]
        [
          ifelse (random 100 < 10)
          [
            set tradePrice (currentBid + random 4 - 3)
          ]
          [
            set tradePrice (currentBid)
          ]    
        ]
      ]  
    ]
  ]
  
  if (ticks < 10002)[
    if (action = "Buy")[set tradePrice price - 1 - random 4]
    if (action = "Sell")[set tradePrice price + 1 + random 4]
  ]
    
  set tradeStatus action
  distOrderHighFrequency
end

;Human Trader
;********************************************************************************************************************************************************************************

to strategyHuman
  if(humanAction = 11)
  [
    set humanAction 1
  ]
  if(humanAction = 22)
  [
    set humanAction 2
  ]
  
  if(humanAction = 1 and (tradeStatus = "Buy" or tradeStatus = "Sell"))
  [
    set removeTrade 1
    set humanAction 11
    
  ]
  
  if(humanAction = 2 and (tradeStatus = "Buy" or tradeStatus = "Sell"))
  [
    set removeTrade 1
    set humanAction 22
  ]
  
  if(BuySize > 10)[
    set buysize 10
  ] 
  
  if(BuySize < 1)[
    set buysize 1
  ] 
  
  if(SellSize > 10)[
    set sellsize 10
  ] 
  
  if(SellSize < 1)[
    set sellsize 1
  ]
  
  if(humanAction = 1)[
    set humanAction 0
    set tradeStatus "Buy"
    set tradeQuantity BuySize
    set tradePrice (currentbid - BuyPriceTicks) 
  ]
  if(humanAction = 2)[
    set humanAction 0
    set tradeStatus "Sell"
    set tradeQuantity SellSize
    set tradePrice (currentask + SellPriceTicks) 
  ]
  if(humanAction = 3)[
    set humanAction 0
    set removeTrade 1
  ]
end

;********************************************************************************************************************************************************************************
;Function for transacting Trades to be run in the Market 
;********************************************************************************************************************************************************************************

to transaction
if( length buyQueue > 0 and length sellQueue > 0)
[
  let firstInBuyQueue first (sort-by [?2 < ?1] buyQueue)
  let firstInSellQueue first (sort-by [?1 < ?2] sellQueue)
  
  set currentAsk int((firstInSellQueue / 10000000000000))
  set currentBid int((firstInBuyQueue / 10000000000000))
  
  let sub1Math int((firstInSellQueue / 10000000))
  let sub2Math int((firstInBuyQueue / 10000000))
  
  let aMath int (firstInBuyQueue / 100)
  let bMath int (firstInSellQueue / 100)
  let cMath int (sub2Math * 100000)
  let dMath int (sub1Math * 100000)
  
  let eMath int (aMath - cMath)
  let fMath int (bMath - dMath)
  
  let orderB int (((sub2Math * 100000) - (currentBid * 100000000000))/ 100000)
  let orderA int (((sub1Math * 100000) - (currentAsk * 100000000000))/ 100000)
  
  let traderB int (aMath - cMath)
  let traderA int (bMath - dMath)
  
  let quantityB array:item arrayQ traderB
  let quantityA array:item arrayQ traderA
  
  let traderBB int (quantityB + (traderB * 100))
  let traderAA int (quantityA + (traderA * 100))
  
  let traderBA int (quantityA + (traderB * 100))
  let traderAB int (quantityB + (traderA * 100))
  
  let quantityDiff int (quantityB - quantityA)
  
  if (currentAsk <= currentBid) [
        
    if ( quantityDiff = 0 ) [
      set buyQueue remove firstInBuyQueue buyQueue
      set sellQueue remove firstInSellQueue sellQueue  
    
      if tradeStatus = "Buy" [
         set tradePrice currentAsk
         set price currentAsk
         set windowS5 lput price windowS5
         set advSpdS5 lput (price * quantityB) advSpdS5
         set advSpdQ5 lput quantityB advSpdQ5
         set tradeWipeQueue lput traderB tradeWipeQueue
         set tradeWipeQueue lput traderA tradeWipeQueue
         set tradeMatch lput traderA tradeMatch
         set tradeMatch lput traderB tradeMatch 
       ]
       if tradeStatus = "Sell" [
          set price currentBid
          set tradePrice currentBid
          set windowS5 lput price windowS5
          set advSpdS5 lput (price * quantityA) advSpdS5
          set advSpdQ5 lput quantityA advSpdQ5
          set tradeWipeQueue lput traderA tradeWipeQueue
          set tradeWipeQueue lput traderB tradeWipeQueue
          set tradeMatch lput traderA tradeMatch
          set tradeMatch lput traderB tradeMatch        
        ]  
      ]      
    
      if ( quantityDiff > 0 ) [
        set sellQueue remove firstInSellQueue sellQueue  
              
        if tradeStatus = "Buy" [
          set tradePrice currentAsk
          set price currentAsk
          set windowS5 lput price windowS5
          set advSpdS5 lput (price * quantityB) advSpdS5
          set advSpdQ5 lput quantityB advSpdQ5
          set tradeWipeQueue lput traderA tradeWipeQueue
          set tradeWipeQueueB lput traderBA tradeWipeQueueB
          set tradeMatch lput traderB tradeMatch
          set tradeMatch lput traderA tradeMatch
          array:set arrayQ traderB quantityDiff
        ]
        if tradeStatus = "Sell" [
          set price currentBid
          set tradePrice currentBid
          set windowS5 lput price windowS5
          set advSpdS5 lput (price * quantityA) advSpdS5
          set advSpdQ5 lput quantityA advSpdQ5
          set tradeWipeQueue lput traderA tradeWipeQueue
          set tradeWipeQueueB lput traderBA tradeWipeQueueB
          set tradeMatch lput traderB tradeMatch
          set tradeMatch lput traderA tradeMatch
          array:set arrayQ traderB quantityDiff
        ]
        transaction 
      ]
      if ( quantityDiff < 0 ) [
        let bQ (sort-by [?2 < ?1] buyQueue)
        set buyQueue remove firstInBuyQueue buyQueue
        
        if tradeStatus = "Buy" [
          set tradePrice currentAsk
          set price currentAsk
          set windowS5 lput price windowS5
          set advSpdS5 lput (price * quantityB) advSpdS5
          set advSpdQ5 lput quantityB advSpdQ5
          set tradeWipeQueue lput traderB tradeWipeQueue
          set tradeWipeQueueA lput traderAB tradeWipeQueueA
          set tradeMatch lput traderA tradeMatch
          set tradeMatch lput traderB tradeMatch
          array:set arrayQ traderA abs(quantityDiff)
        ]
        if tradeStatus = "Sell" [
          set tradePrice currentBid
          set price currentBid
          set windowS5 lput price windowS5
          set advSpdS5 lput (price * quantityA) advSpdS5
          set advSpdQ5 lput quantityA advSpdQ5
          set tradeWipeQueueA lput traderAB tradeWipeQueueA
          set tradeWipeQueue lput traderB tradeWipeQueue
          set tradeMatch lput traderA tradeMatch
          set tradeMatch lput traderB tradeMatch
          array:set arrayQ traderA abs(quantityDiff)
        ]
        transaction 
      ]    
    ]
  ]
end

;********************************************************************************************************************************************************************************
;Calculate Price Returns
;********************************************************************************************************************************************************************************

to calculatePriceReturns 
  set priceReturns lput (ln((item (length movingAverage - 1) movingAverage) / (item (length movingAverage - 30) movingAverage)))  priceReturns
end

to calculateSpread
  set priceSpread lput ((currentSellInterestPrice - currentBuyInterestPrice) / 5)  priceSpread
end

;********************************************************************************************************************************************************************************
;Writing AUDIT TARIL to CSV File
;line output: ORDER ID| HON1 | HON2 | ACCOUNT | BUYSELL | FUNCCODE | TIME | CTI | ASKPRICE | ASKQUANTITY | BIDPRICE | BIDQUANTITY | EXCHANGE | MAX SHOW FLAG | MATCH NUMBER | TANSACTION ID
;********************************************************************************************************************************************************************************

to writeFileTitles
    file-open "OrderBook.csv"
    file-type "ORDER ID"
    file-type ", "
    file-type "HON1"
    file-type ", "
    file-type "HON2"
    file-type ", "
    file-type "ACCOUNT"
    file-type ", "
    file-type "BUYSELL"
    file-type ", "
    file-type "FUNCCODE"
    file-type ", "
    file-type "PRICE"
    file-type ", "
    file-type "QUANTITY"
    file-type ", "
    file-type "TIME"
    file-type ", "
    file-type "CONFTIME"
    file-type ", "
    file-type "CTI"
    file-type ", "
    file-type "ASKPRICE"
    file-type ", "
    file-type "ASKQUANTITY"
    file-type ", "
    file-type "BIDPRICE"
    file-type ", "
    file-type "BIDQUANTITY"
    file-type ", "
    file-type "EXCHANGE"
    file-type ", "
    file-type "MAX SHOW FLAG"
    file-type ", "
    file-type "MATCH NUMBER"
    file-type ", "
    file-type "TRANSACTION ID"
    file-print ", "
    file-close
end 

to writetofile
  if(AuditTrail = true)[
    set tradeExNumber (tradeExNumber + 1)
    let q array:item arrayQ traderNumber
    file-open "OrderBook.csv"
    file-type tradeOrderNumber ;ORDERID
    file-type ", "
    if(tradeStatus = "Cancel")[set modify (modify + 1)]
    file-type word tradeOrderNumber modify;HON1
    file-type ", "
    if(modify > 0)[
      let a (modify - 1)
      file-type word tradeOrderNumber a;HON2
      file-type ", "
    ]
    if(modify = 0)[
      file-type "-";HON2
      file-type ", "
    ]
    if(tradeStatus = "Cancel")[set modify 0]
    file-type traderNumber ;FIRM
    file-type ", "
    file-type buysell ;BUYSELL
    file-type ", "
    if(tradeStatus = "Buy" or tradeStatus = "Sell" )
    [
      file-type "001" ;FUNCCODE
      file-type ", "
    ]
    if(tradeStatus = "Bought" or tradeStatus = "Sold" )
    [
      file-type "105" ;FUNCCODE
      file-type ", "
    ]
    if(tradeStatus = "Cancel" )
    [
      file-type "003" ;FUNCCODE
      file-type ", "
    ]
    file-type ((tradePrice / 5 + 1000)) ;PRICE
    file-type ", "
    file-type q ;QUANTITY
    file-type ", "
    file-type hour ;TIME
    file-type ":" 
    file-type minute 
    file-type ":"
    file-type second
    file-type ", "
    file-type hour ;CONFTIME
    file-type ":" 
    file-type minute 
    file-type ":"
    file-type second
    file-type ", "
    if(typeOfTrader = "OpportunisticTraders")
    [
      file-type "1" ;CTI
      file-type ", "
    ]
    if(typeOfTrader = "FundamentalSellers")
    [
      file-type "2" ;CTI
      file-type ", "
    ]
    if(typeOfTrader = "FundamentalBuyers")
    [
      file-type "3" ;CTI
      file-type ", "
    ]
    if(typeOfTrader = "HighFrequency")
    [
      file-type "4" ;CTI
      file-type ", "
    ]
    if(typeOfTrader = "MarketMakers")
    [
      file-type "5" ;CTI
      file-type ", "
    ]
    if(typeOfTrader = "IRLTrader")
    [
      file-type "9" ;CTI
      file-type ", "
    ]
    if(typeOfTrader = "DumpTrader")
    [
      file-type "7" ;CTI
      file-type ", "
    ]
    if(typeOfTrader = "SmallTraders")
    [
      file-type "6" ;CTI
      file-type ", "
    ]
    if(typeOfTrader = "IcebergTrader")
    [
      file-type "8" ;CTI
      file-type ", "
    ]
    if(typeOfTrader = "HumanTrader")
    [
      file-type "10" ;CTI
      file-type ", "
    ]
    file-type ((currentAsk / 5 + 1000)) ;ASKPRICE
    file-type ", "
    file-type occurrences (((currentAsk / 5 + 1000))* 10) numberAsk ;ASKQUANTITY
    file-type ", "
    file-type ((currentBid / 5 + 1000)) ;BIDPRICE
    file-type ", "
    file-type occurrences (((currentBid / 5 + 1000))* 10) numberBid ;BIDQUANTITY
    file-type ", "
    file-type "CME" ;EXCHANGE
    file-type ", "
    file-type "-" ;MAX SHOW FLAG 
    file-type ", "
    ifelse(item 0 matchId = 0 and item 1 matchId = 0 and item 2 matchId = 0 and item 3 matchId = 0 and item 4 matchId = 0)
    [
      file-type "-" ;MATCH NUMBER
      file-type ", "
    ]
    [
      ifelse(item 4 matchId > 0)[ 
        file-type item 4 matchId ;MATCH NUMBER
        file-type ", "
      ]
      [
        ifelse(item 3 matchId > 0)[ 
          file-type item 3 matchId ;MATCH NUMBER
          file-type ", "
        ]
        [
          ifelse(item 2 matchId > 0)[ 
            file-type item 2 matchId ;MATCH NUMBER
            file-type ", "
          ]
          [
            ifelse(item 1 matchId > 0)[ 
              file-type item 1 matchId ;MATCH NUMBER
              file-type ", "
            ]
            [
              file-type item 0 matchId ;MATCH NUMBER
              file-type ", "
            ]
          ]
        ]
      ]
    ]
    file-type tradeExNumber ;TRANSACTION ID
    file-print ", "
    file-close
  ]
end 


;********************************************************************************************************************************************************************************
;Ploting of Booking to the Histogram, Price, Volume, Volatility, Moving Average ....  
;********************************************************************************************************************************************************************************

to histoSetup1
  let traderQueueCount1 0
  let lengthBuy length buyQueue
  loop [
    let math item traderQueueCount1 buyQueue
    let amath int (math / 100)
    let bmath int((math / 10000000))
    let cmath int (bmath * 100000)
    let traderB int (aMath - cMath)
    let traderBid int((math / 10000000000000))
    let tradeQ array:item arrayQ traderB
    
    repeat tradeQ [
      set numberBid lput (10000 + (traderBid * 2 )) numberBid
    ]
    set traderQueueCount1 (traderQueueCount1 + 1)
    if lengthBuy = traderQueueCount1 [ stop ]
  ]
  set-plot-x-range (10000 + (price * 2) - 50) (10000 + (price * 2) + 50)
end  

to histoSetup2
  let traderQueueCount2 0
  let lengthSell length sellQueue
  loop [
    let math item traderQueueCount2 sellQueue
    let amath int (math / 100)
    let bmath int ((math / 10000000))
    let cmath int (bmath * 100000)
    let traderA int (aMath - cMath)
    let traderAsk int((math / 10000000000000))
    let tradeQ array:item arrayQ traderA
        
    repeat tradeQ [
      set numberAsk lput (10000 + (traderAsk  * 2)) numberAsk
    ]
    set traderQueueCount2 (traderQueueCount2 + 1)
    if lengthSell = traderQueueCount2 [ stop ] 
  ]
  set-plot-x-range (10000 + (price * 2 ) - 50) (10000 + (price * 2) + 50)
end
  
to setup-plots ;;initialize plots
  clear-all-plots
  setup-plot1
  setup-plot3
  if((ticks mod 10) = 0)[
    setup-plot4
  ]
  setup-plot5
  setup-plot8

end
  
to setup-plot1
  set-current-plot "Distribution of Bid/Ask"
  set-plot-x-range (10000 + (price * 2) - 50) (10000 + (price * 2 ) + 50)
  set-plot-y-range 0 10
  set-histogram-num-bars 100
end

to setup-plot3
  set-current-plot "Price"
  plot 1000 + (price / 5)
end

to setup-plot4
  set-current-plot "Volume"
  plot volume
end

to setup-plot5
  set-current-plot "Volatility"
end

to setup-plot8
  set-current-plot "Market Depth"
  plot currentMAV
end

  
to do-plots
  set-current-plot "Distribution of Bid/Ask"
  plot-pen-reset
  set-plot-pen-mode 1
  let n2 numberBid 
  set-current-plot-pen "Bid" 
  histogram n2
  let a2 numberAsk 
  set-current-plot-pen "Ask"
  histogram a2
   
  if(ticks > 10000)
  [
    set-current-plot "Price Returns"
    plot-pen-reset
    set-plot-pen-mode 1
    set-plot-x-range -0.002 .002
    set-histogram-num-bars 31
    histogram priceReturns 
  ]
  
  if(ticks > 10000)
  [
    set-current-plot "Price"
    plot 1000 + (price / 5)
 
  
  if((ticks mod 10) = 8)[
    set-current-plot "Volume"
    set-plot-pen-mode 1
    plot volume
    set totalVolume (totalVolume + volume)
  ]]
  
  if(ticks  > 1000)[
    if((ticks mod 10) = 8)[
     set volatility lput (price / 5 + 1000) volatility
     set pastPrices lput price pastPrices
     let lengthVola length volatility
     if(ticks  > 5000)[
       let subVL sublist volatility (lengthVola - 29) lengthVola
       let avgVL mean subVL
       set movingAverage lput avgVL movingAverage
     ]
     set movingAverageV lput volume movingAverageV
     set currentPriceVol (currentPriceVol + 1)
     if (currentPriceVol > 1000)[set currentPriceVol 1000]
    ]
  ]
   
  set currentVol 0
  if(ticks >= 10300)[
    let lengthVol length volatility
    let subVol sublist volatility (lengthVol - 10) lengthVol  
    let maxVol max subVol
    let minVol min subVol
    set currentVol ln (maxVol / minVol)
    if(((ticks - 10000) mod 300) = 0)[
      set-current-plot "Volatility"
      let lPR length priceReturns
      plot item (lPR - 1) priceReturns
      
    ]
  ]
  
  set currentMAV 2
  if(ticks >= 10000)[
    let lengthMAV length movingAverageV
    let subMAV sublist movingAverageV (lengthMAV - 29) lengthMAV
    let avgMAV mean subMAV
    set currentMAV avgMAV
  ]
  
  set-current-plot "Market Depth"
  set currentBQD 2
  set currentSQD 2
  if(ticks >= 10000)[
    let lengthBQ length numberBid
    let lengthSQ length numberAsk
    set currentBQD lengthBQ
    set currentSQD lengthSQ
    set-current-plot-pen "Bid"
    plot currentBQD
    set-current-plot-pen "Ask"
    plot currentSQD
  ] 

end

;********************************************************************************************************************************************************************************
;Run for one day function
;********************************************************************************************************************************************************************************

to runDay
  let g35 0
  while [g35 <= 122042]
  [
    set g35 (g35 + 1)
    go
  ]
end

;********************************************************************************************************************************************************************************
;Trader State Monitor
;********************************************************************************************************************************************************************************

to-report avgSharesHFTMarketMakers
 report precision (((sum [sharesOwned] of turtles with [typeOfTrader = "HighFrequency"]) +(sum [sharesOwned] of turtles with [typeOfTrader = "MarketMakers"])) / ((count turtles with [typeOfTrader = "HighFrequency"])+ count turtles with [typeOfTrader = "MarketMakers"]))  2
end

to-report avgSharesMarketMakers
 report precision (((sum [sharesOwned] of turtles with [typeOfTrader = "MarketMakers"])) / (count turtles with [typeOfTrader = "MarketMakers"]))  2
end

to-report avgSharesHFT
 ifelse((count turtles with [typeOfTrader = "HighFrequency"]) > 0)
 [report precision (((sum [sharesOwned] of turtles with [typeOfTrader = "HighFrequency"])) / ((count turtles with [typeOfTrader = "HighFrequency"])))  2]
 [report 0]
end

to-report avgFundBuyers
 report precision ((sum [sharesOwned] of turtles with [typeOfTrader = "FundamentalBuyers"])/ (count turtles with [typeOfTrader = "FundamentalBuyers"]))  2
end

to-report avgFundSellers
 report precision ((sum [sharesOwned] of turtles with [typeOfTrader = "FundamentalSellers"])/ (count turtles with [typeOfTrader = "FundamentalSellers"])) 2 
end

to-report avgFund
 report precision ((sum [sharesOwned] of turtles with [typeOfTrader = "FundamentalSellers" or typeOfTrader = "FundamentalBuyers"])/ (count turtles with [typeOfTrader = "FundamentalSellers" or typeOfTrader = "FundamentalBuyers"])) 2 
end

to-report avgSmall
 report precision ((sum [sharesOwned] of turtles with [typeOfTrader = "SmallTraders"])/ (count turtles with [typeOfTrader = "SmallTraders"])) 2 
end

to-report avgOpportunistic
 report precision ((sum [sharesOwned] of turtles with [typeOfTrader = "OpportunisticTraders"])/ (count turtles with [typeOfTrader = "OpportunisticTraders"])) 2 
end

to-report avgDump
 report precision ((sum [sharesOwned] of turtles with [typeOfTrader = "DumpTraders"])/ (count turtles with [typeOfTrader = "DumpTraders"])) 2 
end

to-report avgHuman
 report precision ((sum [sharesOwned] of turtles with [typeOfTrader = "HumanTrader"])/ (count turtles with [typeOfTrader = "HumanTrader"])) 2 
end

to-report accountValueSmall
  report precision ((sum [tradeAccount] of turtles with [typeOfTrader = "SmallTraders"]) / (count turtles with [typeOfTrader = "SmallTraders"])) 2 
end

to-report accountValueFund
  report precision ((sum [tradeAccount] of turtles with [typeOfTrader = "FundamentalSellers" or typeOfTrader = "FundamentalBuyers"]) / (count turtles with [typeOfTrader = "FundamentalSellers" or typeOfTrader = "FundamentalBuyers"])) 2 
end

to-report accountValueOpportunistic
  report precision ((sum [tradeAccount] of turtles with [typeOfTrader = "OpportunisticTraders"]) / (count turtles with [typeOfTrader = "OpportunisticTraders"])) 2 
end

to-report accountValueMarketMakers
  report precision ((sum [tradeAccount] of turtles with [typeOfTrader = "MarketMakers"]) / (count turtles with [typeOfTrader = "MarketMakers"])) 2 
end

to-report accountValueHuman
  report precision ((sum [tradeAccount] of turtles with [typeOfTrader = "HumanTrader"]) / (count turtles with [typeOfTrader = "HumanTrader"])) 2 
end

to-report accountValueBHuman
  report precision ((sum [averageBoughtPrice] of turtles with [typeOfTrader = "HumanTrader"]) / (count turtles with [typeOfTrader = "HumanTrader"])) 2 
end

to-report accountValueSHuman
  report precision ((sum [averageSoldPrice] of turtles with [typeOfTrader = "HumanTrader"]) / (count turtles with [typeOfTrader = "HumanTrader"])) 2 
end

to-report boughtHuman
  report precision ((sum [totalbought] of turtles with [typeOfTrader = "HumanTrader"]) / (count turtles with [typeOfTrader = "HumanTrader"])) 2 
end

to-report soldHuman
  report precision ((sum [totalsold] of turtles with [typeOfTrader = "HumanTrader"]) / (count turtles with [typeOfTrader = "HumanTrader"])) 2 
end


to-report accountValueHFT
ifelse((count turtles with [typeOfTrader = "HighFrequency"]) > 0)
 [ report precision ((sum [tradeAccount] of turtles with [typeOfTrader = "HighFrequency"]) / (count turtles with [typeOfTrader = "HighFrequency"])) 2 ]
 [report 0]
end

to-report volumeTradedSmall
  report precision ((sum [totalBought + totalSold] of turtles with [typeOfTrader = "SmallTraders"])) 0 
end

to-report volumeTradedFund
  report precision ((sum [totalBought + totalSold] of turtles with [typeOfTrader = "FundamentalSellers" or typeOfTrader = "FundamentalBuyers"])) 0 
end

to-report volumeTradedOpportunistic
  report precision ((sum [totalBought + totalSold] of turtles with [typeOfTrader = "OpportunisticTraders"])) 0
end

to-report volumeTradedMM
ifelse((count turtles with [typeOfTrader = "HighFrequency"]) > 0)
  [report precision ((sum [totalBought + totalSold] of turtles with [typeOfTrader = "MarketMakers"])) 0]
  [report 0]
end

to-report volumeTradedHFT
  report precision ((sum [totalBought + totalSold] of turtles with [typeOfTrader = "HighFrequency"])) 0 
end

to-report volumeTraded
  report precision ((sum [totalBought + totalSold] of turtles)) 0 
end

to-report volumeCanceledSmall
  report precision ((sum [totalCanceled] of turtles with [typeOfTrader = "SmallTraders"])) 0 
end

to-report volumeCanceledFund
  report precision ((sum [totalCanceled] of turtles with [typeOfTrader = "FundamentalSellers" or typeOfTrader = "FundamentalBuyers"])) 0 
end

to-report volumeCanceledOpportunistic
  report precision ((sum [totalCanceled] of turtles with [typeOfTrader = "OpportunisticTraders"])) 0
end

to-report volumeCanceledMM
ifelse((count turtles with [typeOfTrader = "HighFrequency"]) > 0)
  [report precision ((sum [totalCanceled] of turtles with [typeOfTrader = "MarketMakers"])) 0]
  [report 0]
end

to-report volumeCanceledHFT
  report precision ((sum [totalCanceled] of turtles with [typeOfTrader = "HighFrequency"])) 0 
end

to-report volumeCanceled
  report precision ((sum [totalCanceled] of turtles)) 0 
end

to-report hour
  report int (((ticks / 300) / 60) + 9.5 - ((10000 / 300) / 60)) 
end

to-report minute
  report int(((((ticks / 300) / 60) + 9.5 - ((10000 / 300) / 60)) mod 1)* 60) 
end

to-report second
  report precision (((((((ticks / 300) / 60) + 9.5 - ((10000 / 300) / 60)) mod 1)* 60) mod 1) * 60) 2
end

;********************************************************************************************************************************************************************************
;Check Trader Account Number is Unique
;********************************************************************************************************************************************************************************

to checkTraderNumberUnique
  let countTradersintradelist length traderListNumbers - 1
  set traderNumber  10 + random 9990
  let countTraderNumber 0
  set checkTraderNumber 1
  loop [
   if(item countTraderNumber traderListNumbers = traderNumber)[set checkTraderNumber 0]
   if(countTraderNumber = countTradersintradelist) [stop]
   set countTraderNumber (countTraderNumber + 1)
  ]
end

;********************************************************************************************************************************************************************************
;Price Distribution Function
;********************************************************************************************************************************************************************************

;Small Trader
;********************************************************************************************************************************************************************************
to distPriceSmallSell
  let randDraw random 100
  set tradePrice price + 10
  if(randDraw < 20 and ticks > 10005)[set tradePrice price - 5 ]
  if(randDraw > 19)[set tradePrice price + 1 ]
  if(randDraw > 31)[set tradePrice price + 2 ]
  if(randDraw > 40)[set tradePrice price + 3 ]
  if(randDraw > 47)[set tradePrice price + 4 ]
  if(randDraw > 54)[set tradePrice price + 5 ]
  if(randDraw > 61)[set tradePrice price + 6 ]
  if(randDraw > 68)[set tradePrice price + 7 ]
  if(randDraw > 73)[set tradePrice price + 8 ]
  if(randDraw > 78 and randDraw < 88)[set tradePrice price + 9]
end

to distPriceSmallBuy
  let randDraw random 100
  set tradePrice price - 10
  if(randDraw < 20 and ticks > 10005)[set tradePrice price + 5 ]
  if(randDraw > 19)[set tradePrice price - 1 ]
  if(randDraw > 31)[set tradePrice price - 2 ]
  if(randDraw > 40)[set tradePrice price - 3 ]
  if(randDraw > 47)[set tradePrice price - 4 ]
  if(randDraw > 54)[set tradePrice price - 5 ]
  if(randDraw > 61)[set tradePrice price - 6 ]
  if(randDraw > 68)[set tradePrice price - 7 ]
  if(randDraw > 73)[set tradePrice price - 8 ]
  if(randDraw > 78 and randDraw < 88)[set tradePrice price - 9]
end

;Fundamental Seller Trader
;********************************************************************************************************************************************************************************
to distPriceFundamentalSell
  let randDraw random 100
  set tradePrice price + 10
  if(randDraw < 14 and ticks > 10005)[set tradePrice price - 5 ]
  if(randDraw > 13)[set tradePrice price + 1 ]
  if(randDraw > 41)[set tradePrice price + 2 ]
  if(randDraw > 52)[set tradePrice price + 3 ]
  if(randDraw > 61)[set tradePrice price + 4 ]
  if(randDraw > 68)[set tradePrice price + 5 ]
  if(randDraw > 75)[set tradePrice price + 6 ]
  if(randDraw > 82)[set tradePrice price + 7 ]
  if(randDraw > 87)[set tradePrice price + 8 ]
  if(randDraw > 92 and randDraw < 97)[set tradePrice price + 9 ]
end

;Fundamental Buyer Trader
;********************************************************************************************************************************************************************************
to distPriceFundamentalBuy
  let randDraw random 100
  set tradePrice price - 10
  if(randDraw < 14 and ticks > 10005)[set tradePrice price + 5 ]
  if(randDraw > 13)[set tradePrice price - 1 ]
  if(randDraw > 41)[set tradePrice price - 2 ]
  if(randDraw > 52)[set tradePrice price - 3 ]
  if(randDraw > 61)[set tradePrice price - 4 ]
  if(randDraw > 68)[set tradePrice price - 5 ]
  if(randDraw > 75)[set tradePrice price - 6 ]
  if(randDraw > 82)[set tradePrice price - 7 ]
  if(randDraw > 87)[set tradePrice price - 8 ]
  if(randDraw > 92 and randDraw < 97)[set tradePrice price - 9 ]
end

;Opportunistic Trader
;********************************************************************************************************************************************************************************
to distPriceOpportunisticBuy
  let randDraw random 100
  set tradePrice price - 10
  if(randDraw < 36 and ticks > 10005)[set tradePrice price + 5 ]
  if(randDraw > 35)[set tradePrice price - 1 ]
  if(randDraw > 55)[set tradePrice price - 2 ]
  if(randDraw > 60)[set tradePrice price - 3 ]
  if(randDraw > 65)[set tradePrice price - 4 ]
  if(randDraw > 70)[set tradePrice price - 5 ]
  if(randDraw > 75)[set tradePrice price - 6 ]
  if(randDraw > 80)[set tradePrice price - 7 ]
  if(randDraw > 85)[set tradePrice price - 8 ]
  if(randDraw > 90 and randDraw < 96)[set tradePrice price - 9 ]
end

to distPriceOpportunisticSell
  let randDraw random 100
  set tradePrice price + 10
  if(randDraw < 36 and ticks > 10005)[set tradePrice price - 5 ]
  if(randDraw > 35)[set tradePrice price + 1 ]
  if(randDraw > 55)[set tradePrice price + 2 ]
  if(randDraw > 60)[set tradePrice price + 3 ]
  if(randDraw > 65)[set tradePrice price + 4 ]
  if(randDraw > 70)[set tradePrice price + 5 ]
  if(randDraw > 75)[set tradePrice price + 6 ]
  if(randDraw > 80)[set tradePrice price + 7 ]
  if(randDraw > 85)[set tradePrice price + 8 ]
  if(randDraw > 90 and randDraw < 96)[set tradePrice price + 9 ]
end

;Market Maker Trader
;********************************************************************************************************************************************************************************
to distPriceMarketMakerBuy
  let randDraw random 100
  set tradePrice price - 10
  if(randDraw < 31 and ticks > 10005)[set tradePrice price + 5 ]
  if(randDraw > 30)[set tradePrice price - 1 ]
  if(randDraw > 48)[set tradePrice price - 2 ]
  if(randDraw > 60)[set tradePrice price - 3 ]
  if(randDraw > 67)[set tradePrice price - 4 ]
  if(randDraw > 73)[set tradePrice price - 5 ]
  if(randDraw > 78)[set tradePrice price - 6 ]
  if(randDraw > 82)[set tradePrice price - 7 ]
  if(randDraw > 86)[set tradePrice price - 8 ]
  if(randDraw > 90 and randDraw < 93)[set tradePrice price - 9 ]
end

to distPriceMarketMakerSell
  let randDraw random 100
  set tradePrice price + 10
  if(randDraw < 31 and ticks > 10005)[set tradePrice price - 5 ]
  if(randDraw > 30)[set tradePrice price + 1 ]
  if(randDraw > 48)[set tradePrice price + 2 ]
  if(randDraw > 60)[set tradePrice price + 3 ]
  if(randDraw > 67)[set tradePrice price + 4 ]
  if(randDraw > 73)[set tradePrice price + 5 ]
  if(randDraw > 78)[set tradePrice price + 6 ]
  if(randDraw > 82)[set tradePrice price + 7 ]
  if(randDraw > 86)[set tradePrice price + 8 ]
  if(randDraw > 90 and randDraw < 93)[set tradePrice price + 9 ]
end

;High Frequency Trader
;********************************************************************************************************************************************************************************
to distPriceHighFrequencyBuy
  let randDraw random 100
  set tradePrice price - 7
  if(randDraw > 5)[set tradePrice price - 6 ]
  if(randDraw > 20)[set tradePrice price - 5 ]
  if(randDraw > 40)[set tradePrice price - 4 ]
  if(randDraw > 55)[set tradePrice price - 3 ]
  if(randDraw > 70)[set tradePrice price - 2 ]
  if(randDraw > 85)[set tradePrice price - 1 ]
  if(randDraw > 98 and ticks > 10005)[set tradePrice price + 5 ]
end

to distPriceHighFrequencySell
  let randDraw random 100
  set tradePrice price + 7
  if(randDraw > 5)[set tradePrice price + 6 ]
  if(randDraw > 20)[set tradePrice price + 5 ]
  if(randDraw > 40)[set tradePrice price + 4 ]
  if(randDraw > 55)[set tradePrice price + 3 ]
  if(randDraw > 70)[set tradePrice price + 2 ]
  if(randDraw > 85)[set tradePrice price + 1 ]
  if(randDraw > 98 and ticks > 10005)[set tradePrice price - 5 ]
end


;********************************************************************************************************************************************************************************
;Order Size Distribution Function
;********************************************************************************************************************************************************************************

;Small Trader
;********************************************************************************************************************************************************************************
to distOrderSmall
  let randDraw random 100
  set tradeQuantity 1
  if(randDraw > 98)[set tradeQuantity 2 ]
end

;Fundamental Trader
;********************************************************************************************************************************************************************************
to distOrderFundamental
  let randDraw random 100
  set tradeQuantity 1
  if(randDraw > 55)[set tradeQuantity 2 ]
  if(randDraw > 65)[set tradeQuantity 3 ]
  if(randDraw > 70)[set tradeQuantity 4 ]
  if(randDraw > 75)[set tradeQuantity 5 ]
  if(randDraw > 90)[set tradeQuantity 6 ]
  if(randDraw > 95)[set tradeQuantity 7 ]
end

;Opportunistic Trader
;********************************************************************************************************************************************************************************
to distOrderOpportunistic
  let randDraw random 100
  set tradeQuantity 1
  if(randDraw > 66)[set tradeQuantity 2 ]
  if(randDraw > 82)[set tradeQuantity 3 ]
  if(randDraw > 87)[set tradeQuantity 4 ]
  if(randDraw > 91)[set tradeQuantity 5 ]
  if(randDraw > 94)[set tradeQuantity 6 ]
  if(randDraw > 97)[set tradeQuantity 7 ]
end

;Market Maker Trader
;********************************************************************************************************************************************************************************
to distOrderMarketMaker
  let randDraw random 100
  set tradeQuantity 1
  if(randDraw > 76)[set tradeQuantity 2 ]
  if(randDraw > 82)[set tradeQuantity 3 ]
  if(randDraw > 88)[set tradeQuantity 4 ]
  if(randDraw > 93)[set tradeQuantity 5 ]
end

;High Frequency Trader
;********************************************************************************************************************************************************************************
to distOrderHighFrequency
  let randDraw random 100
  set tradeQuantity 1
  if(randDraw > 57)[set tradeQuantity 2 ]
  if(randDraw > 76)[set tradeQuantity 3 ]
  if(randDraw > 80)[set tradeQuantity 4 ]
  if(randDraw > 85)[set tradeQuantity 5 ]
  if(randDraw > 90)[set tradeQuantity 6 ]
  if(randDraw > 95)[set tradeQuantity 7 ]
end

;********************************************************************************************************************************************************************************
;Liquidity Measure CDFs
;********************************************************************************************************************************************************************************

to PHIndicatorCDFCalc
  set PHIndicatorValue 0
  let riskPHLevel1 ((currentSellInterestPrice - currentBuyInterestPrice) / 5)
  if(riskPHLevel1 > 0.0)[set PHIndicatorValue 0.0019 ]
  if(riskPHLevel1 > 1.3)[set PHIndicatorValue 0.011 ]
  if(riskPHLevel1 > 1.6)[set PHIndicatorValue 0.0479 ]
  if(riskPHLevel1 > 1.9)[set PHIndicatorValue 0.1282 ]
  if(riskPHLevel1 > 2.2)[set PHIndicatorValue 0.2215 ]
  if(riskPHLevel1 > 2.6)[set PHIndicatorValue 0.3381 ]
  if(riskPHLevel1 > 2.9)[set PHIndicatorValue 0.4786 ]
  if(riskPHLevel1 > 3.2)[set PHIndicatorValue 0.5874 ]
  if(riskPHLevel1 > 3.5)[set PHIndicatorValue 0.6477 ]
  if(riskPHLevel1 > 3.9)[set PHIndicatorValue 0.6775 ]
  if(riskPHLevel1 > 4.2)[set PHIndicatorValue 0.7157 ]
  if(riskPHLevel1 > 4.5)[set PHIndicatorValue 0.7584 ]
  if(riskPHLevel1 > 4.8)[set PHIndicatorValue 0.7863 ]
  if(riskPHLevel1 > 5.2)[set PHIndicatorValue 0.8076 ]
  if(riskPHLevel1 > 5.5)[set PHIndicatorValue 0.8284 ]
  if(riskPHLevel1 > 5.8)[set PHIndicatorValue 0.8484 ]
  if(riskPHLevel1 > 6.1)[set PHIndicatorValue 0.8698 ]
  if(riskPHLevel1 > 6.5)[set PHIndicatorValue 0.8834 ]
  if(riskPHLevel1 > 6.8)[set PHIndicatorValue 0.9067 ]
  if(riskPHLevel1 > 7.1)[set PHIndicatorValue 0.9203 ]
  if(riskPHLevel1 > 7.4)[set PHIndicatorValue 0.9404 ]
  if(riskPHLevel1 > 7.7)[set PHIndicatorValue 0.9534 ]
  if(riskPHLevel1 > 8.38)[set PHIndicatorValue 0.9624 ]
  if(riskPHLevel1 > 8.7)[set PHIndicatorValue 0.9696 ]
  if(riskPHLevel1 > 9.03)[set PHIndicatorValue 0.9799 ]
  if(riskPHLevel1 > 9.35)[set PHIndicatorValue 0.9896 ]
  if(riskPHLevel1 > 9.68)[set PHIndicatorValue 0.999 ]
end

to VPINCDFCalc
  set VPINValue 0
  let riskPHLevel1 (log ((sum (sublist VPINSeries ((length VPINSeries) - 10) ((length VPINSeries) - 1)) / 1575) + 1) 2)
  if(riskPHLevel1 > 0.0)[set VPINValue 0.0147 ]
  if(riskPHLevel1 > 0.00645)[set VPINValue 0.0176 ]
  if(riskPHLevel1 > 0.00968)[set VPINValue 0.0323 ]
  if(riskPHLevel1 > 0.01290)[set VPINValue 0.0528 ]
  if(riskPHLevel1 > 0.01613)[set VPINValue 0.1085 ]
  if(riskPHLevel1 > 0.01936)[set VPINValue 0.2111  ]
  if(riskPHLevel1 > 0.02258)[set VPINValue 0.3167 ]
  if(riskPHLevel1 > 0.02581)[set VPINValue 0.4164 ]
  if(riskPHLevel1 > 0.02903)[set VPINValue 0.5660 ]
  if(riskPHLevel1 > 0.03226)[set VPINValue 0.5560]
  if(riskPHLevel1 > 0.03548)[set VPINValue 0.6393]
  if(riskPHLevel1 > 0.03871)[set VPINValue 0.7302 ]
  if(riskPHLevel1 > 0.04194)[set VPINValue 0.8123 ]
  if(riskPHLevel1 > 0.045161)[set VPINValue 0.8592 ]
  if(riskPHLevel1 > 0.04839)[set VPINValue 0.8974 ]
  if(riskPHLevel1 > 0.05161)[set VPINValue 0.9120 ]
  if(riskPHLevel1 > 0.05484)[set VPINValue 0.9238 ]
  if(riskPHLevel1 > 0.05807)[set VPINValue 0.9326 ]
  if(riskPHLevel1 > 0.06129)[set VPINValue 0.9531 ]
  if(riskPHLevel1 > 0.06452)[set VPINValue 0.9648 ]
  if(riskPHLevel1 > 0.06774)[set VPINValue 0.9736 ]
  if(riskPHLevel1 > 0.07097)[set VPINValue 0.9824 ]
  if(riskPHLevel1 > 0.07752)[set VPINValue 0.9941 ]  
end

to WindowIndicatorCDFCalc
  set WindowLIndicatorValue 0
  if(WindowLIndicator > 0)[set WindowLIndicatorValue 0.0 ]
  if(WindowLIndicator > 1)[set WindowLIndicatorValue 0.0 ]
  if(WindowLIndicator > 2)[set WindowLIndicatorValue 0.0 ]
  if(WindowLIndicator > 3)[set WindowLIndicatorValue 0.0729 ]
  if(WindowLIndicator > 4)[set WindowLIndicatorValue 0.4888 ]
  if(WindowLIndicator > 5)[set WindowLIndicatorValue 0.6592 ]
  if(WindowLIndicator > 6)[set WindowLIndicatorValue 0.8296]
  if(WindowLIndicator > 7)[set WindowLIndicatorValue 0.9114 ]
  if(WindowLIndicator > 8)[set WindowLIndicatorValue 0.9523 ]
  if(WindowLIndicator > 9)[set WindowLIndicatorValue 0.9727 ]
  if(WindowLIndicator > 10)[set WindowLIndicatorValue 0.9796 ]
  if(WindowLIndicator > 11)[set WindowLIndicatorValue 0.9864 ]
  if(WindowLIndicator > 12)[set WindowLIndicatorValue 0.9932 ]
  if(WindowLIndicator > 13)[set WindowLIndicatorValue 0.9964 ]
  if(WindowLIndicator > 14)[set WindowLIndicatorValue 0.9985 ]
  if(WindowLIndicator > 15)[set WindowLIndicatorValue 0.9994 ]
end

to advSpreadCDFCalc
  set advSpreadValue 0
  if(advSpread > 0)[set advSpreadValue 0.0 ]
  if(advSpread > 0.01)[set advSpreadValue 0.0 ]
  if(advSpread > 0.02)[set advSpreadValue 0.0 ]
  if(advSpread > 0.03)[set advSpreadValue 0.0729 ]
  if(advSpread > 0.04)[set advSpreadValue 0.4888 ]
  if(advSpread > 0.05)[set advSpreadValue 0.6592 ]
  if(advSpread > 0.06)[set advSpreadValue 0.8296]
  if(advSpread > 0.07)[set advSpreadValue 0.9114 ]
  if(advSpread > 0.08)[set advSpreadValue 0.9523 ]
  if(advSpread > 0.09)[set advSpreadValue 0.9727 ]
  if(advSpread > 0.01)[set advSpreadValue 0.9796 ]
  if(advSpread > 0.011)[set advSpreadValue 0.9864 ]
  if(advSpread > 0.012)[set advSpreadValue 0.9932 ]
  if(advSpread > 0.013)[set advSpreadValue 0.9964 ]
  if(advSpread > 0.014)[set advSpreadValue 0.9985 ]
  if(advSpread > 0.015)[set advSpreadValue 0.9994 ]
end

to-report occurrences [x the-list]
  report reduce
    [ifelse-value (?2 = x) [?1 + 1] [?1]] (fput 0 the-list)
end

;;;;;;;;;;;;;;;;;;;;;;
;; HubNet Procedures
;;;;;;;;;;;;;;;;;;;;;;

to listen-clients
  while [hubnet-message-waiting?]
  [
    hubnet-fetch-message
    ifelse hubnet-enter-message?
    [ add-player ]
    [
      ifelse hubnet-exit-message?
     [ remove-player ]
     [  
        if hubnet-message-tag = "BuySize"
        [
          ask players with [ user-name = hubnet-message-source ]
          [ ifelse(is-number? hubnet-message)[set buysize hubnet-message][carefully [ set buysize read-from-string hubnet-message] [ set buysize 10 ]]]
        ]
        if hubnet-message-tag = "BuyPriceTicks"
        [
          ask players with [ user-name = hubnet-message-source ]
          [ifelse(is-number? hubnet-message)[set buypriceticks hubnet-message][carefully [ set buypriceticks read-from-string hubnet-message] [ set buypriceticks -10 ]]]
            
        ]
        if hubnet-message-tag = "SellSize"
        [
          ask players with [ user-name = hubnet-message-source ]
          [ifelse(is-number? hubnet-message)[set sellsize hubnet-message][carefully [ set sellsize read-from-string hubnet-message] [ set sellsize 10 ]]]
        ]
        if hubnet-message-tag = "SellPriceTicks"
        [
          ask players with [ user-name = hubnet-message-source ]
          [ifelse(is-number? hubnet-message)[set sellpriceticks hubnet-message][carefully [ set sellpriceticks read-from-string hubnet-message] [ set sellpriceticks -10 ]]]
        ]
        if hubnet-message-tag = "Buy"
        [
          ask players with [ user-name = hubnet-message-source ]
          [ set humanAction 1]
        ]
        if hubnet-message-tag = "Sell"
        [
          ask players with [ user-name = hubnet-message-source ]
          [ set humanAction 2]
        ]
        if hubnet-message-tag = "Cancel"
        [
          ask players with [ user-name = hubnet-message-source ]
          [ set humanAction 3]
        ]
      ]
    ]
  ]
end

;; when a client logs in make a new player
;; and give it the default attributes
to add-player
  create-players 1
  [
    set user-name hubnet-message-source
    setup-humantraders 
  ]
end

to remove-player
  ask players with [ user-name = hubnet-message-source ]
    [ die ]
end

to setup-humantraders  ;; player procedure for setup
  set typeOfTrader "HumanTrader"
  set speed traderSpeed
  set tradeSpeedAdjustment 0
  set tradeStatus "Neutral"
  set price 1350
  set orderNumber 0
  set countticks 0
  set tradeAccount 0
  set sharesOwned 0
  set totalBought 0
  set totalSold 0
  set color brown
  set averageBoughtPrice 0
  set averageSoldPrice 0
  let dumpTimer 0
  set marketPauseTickCount 0
  set totalCanceled 0
  set buysell "B"
  set modify 0
  set matchId [0 0 0 0 0]
  set tradeQ2 0
  set removeTrade 0
  set humanAction 0
  set sellpriceticks 0
  set buypriceticks 0
  set buysize 0
  set sellsize 0
end

;; update the monitors on the client
to send-student-info ;; player procedure
  hubnet-send user-name "Price" (1000 + (price / 5))
  hubnet-send user-name "Current Bid" (1000 + (currentBid / 5))
  hubnet-send user-name "Current Ask" (1000 + (currentAsk / 5))
  hubnet-send user-name "Trader Position" sharesOwned
  hubnet-send user-name "Account Value" precision tradeAccount 2
  hubnet-send user-name "Account Value" precision tradeAccount 2
end

to eat-bugs
  ask players
  [
    send-student-info
  ]
end










  

@#$#@#$#@
GRAPHICS-WINDOW
449
364
613
549
7
7
10.3
1
10
1
1
1
0
1
1
1
-7
7
-7
7
0
0
1
ticks

BUTTON
4
10
104
43
Setup Model
setup
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL

MONITOR
395
37
490
86
Price
1000 + (price / 5)
2
1
12

BUTTON
132
10
195
43
NIL
go
T
1
T
OBSERVER
NIL
NIL
NIL
NIL

MONITOR
282
59
362
104
Current Bid
1000 + (currentBid / 5)
17
1
11

MONITOR
523
59
602
104
Current Ask
1000 + (currentAsk / 5)
17
1
11

PLOT
242
112
642
303
Distribution of Bid/Ask
Price
# of contracts
0.0
10.0
0.0
10.0
true
false
PENS
"Bid" 1.0 0 -16777216 true
"Ask" 1.0 0 -2674135 true

TEXTBOX
401
11
551
31
Market State
16
0.0
1

TEXTBOX
-15
307
1405
330
===========================================================================
16
0.0
1

SLIDER
11
113
201
146
#_Fundamental_Sellers
#_Fundamental_Sellers
0
100
40
5
1
NIL
HORIZONTAL

SLIDER
11
153
201
186
#_Fundamental_Buyers
#_Fundamental_Buyers
0
100
40
5
1
NIL
HORIZONTAL

SLIDER
11
193
201
226
#_Market_Makers
#_Market_Makers
1
50
9
1
1
NIL
HORIZONTAL

SLIDER
11
73
201
106
#_Small_Traders
#_Small_Traders
0
1000
200
10
1
NIL
HORIZONTAL

SLIDER
11
273
201
306
#_High_Frequency_Traders
#_High_Frequency_Traders
0
10
3
1
1
NIL
HORIZONTAL

SLIDER
11
233
201
266
#_Opportunistic_Traders
#_Opportunistic_Traders
1
500
183
1
1
NIL
HORIZONTAL

TEXTBOX
40
47
190
65
Numbers of Traders
16
0.0
1

PLOT
3
353
434
473
Price
Time
Price
0.0
10.0
1260.0
1280.0
true
false
PENS
"default" 1.0 0 -16777216 true

PLOT
3
472
434
592
Volume
Time
Qty
0.0
10.0
0.0
10.0
true
false
PENS
"default" 1.0 0 -16777216 true

PLOT
224
592
655
712
Volatility
Time
NIL
0.0
10.0
-5.0E-4
5.0E-4
true
false
PENS
"default" 1.0 0 -16777216 true

MONITOR
377
471
434
516
Volume
int (totalVolume)
17
1
11

MONITOR
596
592
655
637
Volatility
precision (item (length priceReturns - 1) priceReturns) 4
17
1
11

MONITOR
377
353
434
398
Price
1000 + (price / 5)
17
1
11

PLOT
434
353
865
473
Market Depth
Time
Qty
0.0
10.0
0.0
10.0
true
true
PENS
"Bid" 1.0 0 -16777216 true
"Ask" 1.0 0 -2674135 true

PLOT
434
472
865
592
Price Returns
NIL
NIL
0.5
0.5
0.0
10.0
true
false
PENS
"default" 1.0 0 -16777216 true

TEXTBOX
371
325
521
345
Market Measures
16
0.0
1

MONITOR
680
45
730
90
H
hour
17
1
11

MONITOR
730
45
780
90
M
minute
17
1
11

MONITOR
780
45
830
90
S
second
17
1
11

TEXTBOX
711
125
861
145
Trading Tools
16
0.0
1

SLIDER
676
162
848
195
traderSpeed
traderSpeed
5
100
25
5
1
NIL
HORIZONTAL

TEXTBOX
735
16
885
36
Clock
16
0.0
1

BUTTON
677
209
851
242
NIL
tick-advance 1000
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL

@#$#@#$#@
WHAT IS IT?
-----------
This is a model of traders (agents) trading in futures market (emini S&P Futures, CME), placing orders in to a limit order book. The agents are of 6 different veriaties:

Fundamental Buyers and Sellers – take a long or short positions on the asset during the entire duration of the markets exists and trade with a low frequency but as a group make up 13% of all the transactions and 20% of the trade volume. 

Intermediaries – take the position of straddling both sides of the market by taking long and short positions on an asset by implementing trades that are meant to give the market liquidity. They trade with medium frequency and make up 7% of all the transactions and 10% of the trade volume. 

Opportunistic – take a long or short positions on the asset during the duration of the markets like a fundamental trader but implement trading strategies that make them resemble a Market Maker by acting on strategies that are meant to give them a market neutral position by the end of the day. These traders make up 25% of all the transactions and 33% of the trade volume. 

High Frequency - take the position of straddling both sides of the market by taking long and short positions on an asset for short periods of time in an attempt to make profits from executions while never holding a significant position. They trade with high frequency and make up 53% of all the transactions and 36% of the trade volume. 

Noise Traders – take either a long or short positions on the asset during the entire duration of the markets exists and trade with a very low frequency and only make up 2 % of all the transactions and 1% of the trade volume. 


HOW IT WORKS
------------
At the model set up with a given number of each types of traders and the amount of time they will leave an order into the market, before canceling it. The model is then run and at every tick each agent is examined to see if they can place an order or cancel/modify an order. The agent trading strategies determine what prices and quntites each agent will decided top place depending on market variables (price, moving average, depth of order book, ratio of order book, volatility, ....). 

The market is set up as a 'Price-Time Priority' basis. The highest bid order and the lowest ask order consitutes the best market for the given futures contract.
    - If it is a buy order it is matched against sell orders sorted by their price            followed by creation time. 
    - If it is a sell order it is matched against buy orders sorted by their price            followed by creation time.


HOW TO USE IT
-------------
Pressing the SETUP button will clear all transcations and initialize the traders.
Pressing the GO button will start the trades (or if you want to advance the model for one day Press the 'Run for 1 Day' Button). Once the GO button is selected the traders are given the opportunitiy to place orders into the market before it opens, to fill the order book 

The 'Market Sentiment Adjustment' slider allows the you to pick a market directional trand. The 'Market Volatility' allows the user to turn the E-Mini Market Volatility switch on to make to price movements follow the price retuen volatility of a the tick price movement data on a minutes basis. The marketVolatility input box can be reset to whatever price returns data is used (It is currentl;y set to 300 ticks which equals a min)

The 'Number of Traders' section allows you to use a slider bar to input the number of trades of each time they would like to see in the Market. The 'Length of Open Trade' slider can be used to determine the length a order is left in the limit order book for each trader type.

The 'Makret Flash Crash' has three switchs w/ a slider. The 'Flash' switch  turns on a market short sale position being unloaded at a certain rate that is sleded from the sliders bar. The total quantity the traders wants to unload can set in the input box.  The 'Market Pause' switch is automaticly turnned on turing sharp price falls and will help reset the market. The 'Clear Dump Trader' is a switch to clear the the position of the market orders exicuted for flash market trader.   

CREDITS AND REFERENCES
----------------------
Copyright 2012 Mark Paddrik, Roy Hayes, Andrew Todd
@#$#@#$#@
default
true
0
Polygon -7500403 true true 150 5 40 250 150 205 260 250

airplane
true
0
Polygon -7500403 true true 150 0 135 15 120 60 120 105 15 165 15 195 120 180 135 240 105 270 120 285 150 270 180 285 210 270 165 240 180 180 285 195 285 165 180 105 180 60 165 15

arrow
true
0
Polygon -7500403 true true 150 0 0 150 105 150 105 293 195 293 195 150 300 150

box
false
0
Polygon -7500403 true true 150 285 285 225 285 75 150 135
Polygon -7500403 true true 150 135 15 75 150 15 285 75
Polygon -7500403 true true 15 75 15 225 150 285 150 135
Line -16777216 false 150 285 150 135
Line -16777216 false 150 135 15 75
Line -16777216 false 150 135 285 75

bug
true
0
Circle -7500403 true true 96 182 108
Circle -7500403 true true 110 127 80
Circle -7500403 true true 110 75 80
Line -7500403 true 150 100 80 30
Line -7500403 true 150 100 220 30

butterfly
true
0
Polygon -7500403 true true 150 165 209 199 225 225 225 255 195 270 165 255 150 240
Polygon -7500403 true true 150 165 89 198 75 225 75 255 105 270 135 255 150 240
Polygon -7500403 true true 139 148 100 105 55 90 25 90 10 105 10 135 25 180 40 195 85 194 139 163
Polygon -7500403 true true 162 150 200 105 245 90 275 90 290 105 290 135 275 180 260 195 215 195 162 165
Polygon -16777216 true false 150 255 135 225 120 150 135 120 150 105 165 120 180 150 165 225
Circle -16777216 true false 135 90 30
Line -16777216 false 150 105 195 60
Line -16777216 false 150 105 105 60

car
false
0
Polygon -7500403 true true 300 180 279 164 261 144 240 135 226 132 213 106 203 84 185 63 159 50 135 50 75 60 0 150 0 165 0 225 300 225 300 180
Circle -16777216 true false 180 180 90
Circle -16777216 true false 30 180 90
Polygon -16777216 true false 162 80 132 78 134 135 209 135 194 105 189 96 180 89
Circle -7500403 true true 47 195 58
Circle -7500403 true true 195 195 58

circle
false
0
Circle -7500403 true true 0 0 300

circle 2
false
0
Circle -7500403 true true 0 0 300
Circle -16777216 true false 30 30 240

cow
false
0
Polygon -7500403 true true 200 193 197 249 179 249 177 196 166 187 140 189 93 191 78 179 72 211 49 209 48 181 37 149 25 120 25 89 45 72 103 84 179 75 198 76 252 64 272 81 293 103 285 121 255 121 242 118 224 167
Polygon -7500403 true true 73 210 86 251 62 249 48 208
Polygon -7500403 true true 25 114 16 195 9 204 23 213 25 200 39 123

cylinder
false
0
Circle -7500403 true true 0 0 300

dot
false
0
Circle -7500403 true true 90 90 120

face happy
false
0
Circle -7500403 true true 8 8 285
Circle -16777216 true false 60 75 60
Circle -16777216 true false 180 75 60
Polygon -16777216 true false 150 255 90 239 62 213 47 191 67 179 90 203 109 218 150 225 192 218 210 203 227 181 251 194 236 217 212 240

face neutral
false
0
Circle -7500403 true true 8 7 285
Circle -16777216 true false 60 75 60
Circle -16777216 true false 180 75 60
Rectangle -16777216 true false 60 195 240 225

face sad
false
0
Circle -7500403 true true 8 8 285
Circle -16777216 true false 60 75 60
Circle -16777216 true false 180 75 60
Polygon -16777216 true false 150 168 90 184 62 210 47 232 67 244 90 220 109 205 150 198 192 205 210 220 227 242 251 229 236 206 212 183

fish
false
0
Polygon -1 true false 44 131 21 87 15 86 0 120 15 150 0 180 13 214 20 212 45 166
Polygon -1 true false 135 195 119 235 95 218 76 210 46 204 60 165
Polygon -1 true false 75 45 83 77 71 103 86 114 166 78 135 60
Polygon -7500403 true true 30 136 151 77 226 81 280 119 292 146 292 160 287 170 270 195 195 210 151 212 30 166
Circle -16777216 true false 215 106 30

flag
false
0
Rectangle -7500403 true true 60 15 75 300
Polygon -7500403 true true 90 150 270 90 90 30
Line -7500403 true 75 135 90 135
Line -7500403 true 75 45 90 45

flower
false
0
Polygon -10899396 true false 135 120 165 165 180 210 180 240 150 300 165 300 195 240 195 195 165 135
Circle -7500403 true true 85 132 38
Circle -7500403 true true 130 147 38
Circle -7500403 true true 192 85 38
Circle -7500403 true true 85 40 38
Circle -7500403 true true 177 40 38
Circle -7500403 true true 177 132 38
Circle -7500403 true true 70 85 38
Circle -7500403 true true 130 25 38
Circle -7500403 true true 96 51 108
Circle -16777216 true false 113 68 74
Polygon -10899396 true false 189 233 219 188 249 173 279 188 234 218
Polygon -10899396 true false 180 255 150 210 105 210 75 240 135 240

house
false
0
Rectangle -7500403 true true 45 120 255 285
Rectangle -16777216 true false 120 210 180 285
Polygon -7500403 true true 15 120 150 15 285 120
Line -16777216 false 30 120 270 120

leaf
false
0
Polygon -7500403 true true 150 210 135 195 120 210 60 210 30 195 60 180 60 165 15 135 30 120 15 105 40 104 45 90 60 90 90 105 105 120 120 120 105 60 120 60 135 30 150 15 165 30 180 60 195 60 180 120 195 120 210 105 240 90 255 90 263 104 285 105 270 120 285 135 240 165 240 180 270 195 240 210 180 210 165 195
Polygon -7500403 true true 135 195 135 240 120 255 105 255 105 285 135 285 165 240 165 195

line
true
0
Line -7500403 true 150 0 150 300

line half
true
0
Line -7500403 true 150 0 150 150

pentagon
false
0
Polygon -7500403 true true 150 15 15 120 60 285 240 285 285 120

person
false
0
Circle -7500403 true true 110 5 80
Polygon -7500403 true true 105 90 120 195 90 285 105 300 135 300 150 225 165 300 195 300 210 285 180 195 195 90
Rectangle -7500403 true true 127 79 172 94
Polygon -7500403 true true 195 90 240 150 225 180 165 105
Polygon -7500403 true true 105 90 60 150 75 180 135 105

plant
false
0
Rectangle -7500403 true true 135 90 165 300
Polygon -7500403 true true 135 255 90 210 45 195 75 255 135 285
Polygon -7500403 true true 165 255 210 210 255 195 225 255 165 285
Polygon -7500403 true true 135 180 90 135 45 120 75 180 135 210
Polygon -7500403 true true 165 180 165 210 225 180 255 120 210 135
Polygon -7500403 true true 135 105 90 60 45 45 75 105 135 135
Polygon -7500403 true true 165 105 165 135 225 105 255 45 210 60
Polygon -7500403 true true 135 90 120 45 150 15 180 45 165 90

square
false
0
Rectangle -7500403 true true 30 30 270 270

square 2
false
0
Rectangle -7500403 true true 30 30 270 270
Rectangle -16777216 true false 60 60 240 240

star
false
0
Polygon -7500403 true true 151 1 185 108 298 108 207 175 242 282 151 216 59 282 94 175 3 108 116 108

target
false
0
Circle -7500403 true true 0 0 300
Circle -16777216 true false 30 30 240
Circle -7500403 true true 60 60 180
Circle -16777216 true false 90 90 120
Circle -7500403 true true 120 120 60

tree
false
0
Circle -7500403 true true 118 3 94
Rectangle -6459832 true false 120 195 180 300
Circle -7500403 true true 65 21 108
Circle -7500403 true true 116 41 127
Circle -7500403 true true 45 90 120
Circle -7500403 true true 104 74 152

triangle
false
0
Polygon -7500403 true true 150 30 15 255 285 255

triangle 2
false
0
Polygon -7500403 true true 150 30 15 255 285 255
Polygon -16777216 true false 151 99 225 223 75 224

truck
false
0
Rectangle -7500403 true true 4 45 195 187
Polygon -7500403 true true 296 193 296 150 259 134 244 104 208 104 207 194
Rectangle -1 true false 195 60 195 105
Polygon -16777216 true false 238 112 252 141 219 141 218 112
Circle -16777216 true false 234 174 42
Rectangle -7500403 true true 181 185 214 194
Circle -16777216 true false 144 174 42
Circle -16777216 true false 24 174 42
Circle -7500403 false true 24 174 42
Circle -7500403 false true 144 174 42
Circle -7500403 false true 234 174 42

turtle
true
0
Polygon -10899396 true false 215 204 240 233 246 254 228 266 215 252 193 210
Polygon -10899396 true false 195 90 225 75 245 75 260 89 269 108 261 124 240 105 225 105 210 105
Polygon -10899396 true false 105 90 75 75 55 75 40 89 31 108 39 124 60 105 75 105 90 105
Polygon -10899396 true false 132 85 134 64 107 51 108 17 150 2 192 18 192 52 169 65 172 87
Polygon -10899396 true false 85 204 60 233 54 254 72 266 85 252 107 210
Polygon -7500403 true true 119 75 179 75 209 101 224 135 220 225 175 261 128 261 81 224 74 135 88 99

wheel
false
0
Circle -7500403 true true 3 3 294
Circle -16777216 true false 30 30 240
Line -7500403 true 150 285 150 15
Line -7500403 true 15 150 285 150
Circle -7500403 true true 120 120 60
Line -7500403 true 216 40 79 269
Line -7500403 true 40 84 269 221
Line -7500403 true 40 216 269 79
Line -7500403 true 84 40 221 269

x
false
0
Polygon -7500403 true true 270 75 225 30 30 225 75 270
Polygon -7500403 true true 30 75 75 30 270 225 225 270

@#$#@#$#@
NetLogo 4.1.3
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
<experiments>
  <experiment name="experiment" repetitions="1" runMetricsEveryStep="true">
    <go>go</go>
    <metric>count turtles</metric>
    <enumeratedValueSet variable="#_Fundamental_Sellers">
      <value value="40"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="smallTradeLength">
      <value value="10000"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="highTradeLength">
      <value value="6"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="ClearDumpTrader">
      <value value="false"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="MarketPause">
      <value value="false"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="#_Opportunistic_Traders">
      <value value="183"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="fundMarketTradeLength">
      <value value="2000"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="#_Small_Traders">
      <value value="200"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="SellOffTotal">
      <value value="-800"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="marketMakerTradeLength">
      <value value="300"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="#_Market_Makers">
      <value value="6"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="opportunisticTradeLength">
      <value value="1200"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="#_Fundamental_Buyers">
      <value value="40"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="#_High_Frequency_Traders">
      <value value="3"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="Flash">
      <value value="false"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="LargeTradeSize">
      <value value="91"/>
    </enumeratedValueSet>
  </experiment>
</experiments>
@#$#@#$#@
MONITOR
86
92
187
141
Trader Position
NIL
3
1

MONITOR
207
92
299
141
Account Value
NIL
1
1

MONITOR
156
14
213
63
Price
NIL
3
1

MONITOR
55
27
130
76
Current Bid
NIL
3
1

MONITOR
241
28
320
77
Current Ask
NIL
3
1

BUTTON
36
157
99
218
Buy
NIL
NIL
1
T
OBSERVER
NIL
NIL

BUTTON
36
224
99
283
Sell
NIL
NIL
1
T
OBSERVER
NIL
NIL

BUTTON
256
158
325
284
Cancel
NIL
NIL
1
T
OBSERVER
NIL
NIL

INPUTBOX
104
158
164
218
BuySize
0
1
0
Number

INPUTBOX
169
158
250
218
BuyPriceTicks
0
1
0
Number

INPUTBOX
104
223
164
283
SellSize
0
1
0
Number

INPUTBOX
169
223
250
283
SellPriceTicks
0
1
0
Number

@#$#@#$#@
default
0.0
-0.2 0 0.0 1.0
0.0 1 1.0 0.0
0.2 0 0.0 1.0
link direction
true
0
Line -7500403 true 150 150 90 180
Line -7500403 true 150 150 210 180

@#$#@#$#@
0
@#$#@#$#@
