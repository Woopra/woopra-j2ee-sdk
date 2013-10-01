Track customers directly in Java using Woopra's Java SDK

The SDK can be used both for front-end and back-end tracking. In either cases, you should setup the tracker SDK first. To do so, configure the tracker instance as follows (replace <code>mybusiness.com</code> with your website as registered on Woopra, and specify as parameters the instances of your Servlet's HttpServletRequest and HttpServletResponse):

``` java
WoopraTracker woopra = new WoopraTracker(request, response);
woopra.config(WoopraTracker.DOMAIN, "mybusiness.com");
```

You can update your idle timeout (default: 30 seconds) by updating the timeout property in your <code>WoopraTracker</code> instance (NB: this could also have been done in the step above, by adding all the properties you wish to configure to the array):

``` java
woopra.config(WoopraTracker.IDLE_TIMEOUT, 15000); // in milliseconds
```

If you don't want to keep the user online on Woopra when they don't commit any event between the last event and the <code>IDLE_TIMEOUT</code>, you can disable auto pings (auto ping only matters for front-end tracking).

``` java
woopra.config(WoopraTracker.PING, false); // default is true
```

To add custom visitor properties, you should create an instance of WoopraVisitor, configure it, then pass it to the <code>identify(user)</code> function:

``` java
WoopraUser user = new WoopraUser();
user.setProperty("name", "User Name");
user.setProperty("email", "user@company.com");
user.setProperty("company", "User Business");
woopra.identify(user);
```

If you wish to track page views, first call <code>track()</code>, and finally calling <code>woopraCode()</code> in your page header will insert the woopra javascript tracker:

``` java
<head>
   ...
  <% woopra.track().woopraCode(); %>
</head>
```

You can always track events through front-end later in the page. With all the previous steps done at once, it should look like:

``` java
<html>
   <head>
      ...
      <%
         woopra = new WoopraTracker(request, response);
         woopra.config(WoopraTracker.DOMAIN, "mybusiness.com").identify(user).track().woopraCode();
      %>
   </head>
   <body>
      ...
      <%
         event = new WoopraEvent("play");
         event.setProperty("artist", "Dave Brubeck");
         event.setProperty("song", "Take Five");
         event.setProperty("genre", "Jazz");
         woopra.track(event);
      %>
      ...

   </body>
</html>
```

To track a custom event through back-end, just specify the additional parameter <code>true</code> in the <code>track()</code> functions.

``` java
woopra.track(event, true);
```

If you identify the user after the last tracking event, don't forget to <code>push()</code> the update to Woopra:

``` java
woopra.identify(user).push();

//or, to push through back-end:
woopra.identify(user).push(true);
```

If you're only going to be tracking through the back-end, set the cookie (before the headers are sent):

``` java
woopra.setWoopraCookie();
```

And to make sure you're sending the user's IP Address, set it manually doing (the default value is <code>request.getRemoteAddr()</code>):

``` java
woopra.config(WoopraTracker.IP_ADDRESS, "74.125.224.72");
```
