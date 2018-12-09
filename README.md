# Tillr2
This Android mobile application allows a user to manage point of sale transactions; 
(Wikipedia,07-12-2018) "The point of sale or point of purchase is the time and place where a retail transaction is completed.
At the point of sale, the merchant calculates the amount owed by the customer, indicates that amount,
may prepare an invoice for the customer, and indicates the options for the customer to make payment."

It's intended to be quick and easy to use; this is achieved by incorparating Android's intuitive Material Design GUI Components.


![Dash board](https://github.com/wastedMynd/Tillr2/blob/master/wiki%20asserts/Screenshot%20(Dashboard).png) 
|![Create New Item](https://github.com/wastedMynd/Tillr2/blob/master/wiki%20asserts/Screenshot%20(CreateNewItem).png)
|![View Items](https://github.com/wastedMynd/Tillr2/blob/master/wiki%20asserts/Screenshot(ViewItems).png)
|![Place An Order](https://github.com/wastedMynd/Tillr2/blob/master/wiki%20asserts/Screenshot(PlaceAnOrder).png)
|![Pay for Order](https://github.com/wastedMynd/Tillr2/blob/master/wiki%20asserts/Screenshot(PlaceAnOrder%23Payment).png)
|![Register Order](https://github.com/wastedMynd/Tillr2/blob/master/wiki%20asserts/Screenshot(PlaceAnOrder%23Save).png)
|![Prompt For Another](https://github.com/wastedMynd/Tillr2/blob/master/wiki%20asserts/Screenshot(PlaceAnOrder%23Another).png)
|![Dashboard Updated](https://github.com/wastedMynd/Tillr2/blob/master/wiki%20asserts/Screenshot%20(Dashboard%23Updated).png)
|![View Orders](https://github.com/wastedMynd/Tillr2/blob/master/wiki%20asserts/Screenshot(ViewAllOrders).png)

There maybe some state leaks, causing the app to crash; this is caused by(query databases on the main thread).
Potential siting for these leaks are the dashboard(noted when this fragment changes before dashboard task(s) complete),
and when viewing orders(Something happens): Views depending on background queried data;get accessed out of context(when the fragment is no longer in Scope there). 
