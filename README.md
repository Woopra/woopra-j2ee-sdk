Track customers directly in the J2EE Web Framework using Woopra's J2EE SDK

The SDK can be used both for front-end and back-end tracking. In either cases, the first step is to setup the tracker SDK in your Servlets. For example, if you want to set up tracking with Woopra on your homepage, the Servlet should look like:

``` java
public class Homepage extends HttpServlet {
   public void doGet(HttpServletRequest request, HttpServletResponse response) {
      WoopraTracker woopra = new WoopraTrackerEE(request, response);
      woopra.config(WoopraTracker.DOMAIN, "mybusiness.com");

      // Your code here...

      // Set the Woopra Cookie before sending any headers
      woopra.setWoopraCookie();
      // When you're done setting up your WoopraTracker object, set an attribute containing the
      // value of woopra.woopraCode() among all the other attributes you are passing to the jsp.
      request.setAttribute("woopraCode", woopra.woopraCode());
      this.getServletContext().getRequestDispatcher("/WEB-INF/homepage.jsp").forward(request, response);
```
You can also customize all the properties of the tracker by repeating that step with different parameters. For example, to also update your idle timeout (default: 30 seconds):
``` java
woopra.config(WoopraTracker.IDLE_TIMEOUT, 15000); // in milliseconds
```
Configuration can also be done in one single step, by adding all the properties you wish to configure to a 2D Array:
``` java
woopra.config(new Object[][] {
   {WoopraTracker.DOMAIN, "4ltrophy.campus.ecp.fr"},
   {WoopraTracker.IDLE_TIMEOUT, 15000},
   {WoopraTracker.PING, false},
});
```
To add custom visitor properties, you should pass a 2D Array to the <code>identify(String[][] user)</code> function:
``` java
woopra.identify(new String[][] {
   {"name", "User Name"},
   {"email", "user@company.com"},
   {"company", "User Business"}
});
```
If you wish to identify a user without any tracking event, don't forget to push() the update to Woopra:
``` java
woopra.identify(user).push();
//or, to push through back-end:
woopra.identify(user).push(true);
```
If you wish to track page views, just call <code>track()</code>:
``` java
woopra.track();
//Or, for back-end tracking, just add the optional parameter true:
woopra.track(true);
```
You can also track custom events through the front-end or the back-end. With all the previous steps done at once, your Servlet should look like:
``` java
public class Homepage extends HttpServlet {
   public void doGet(HttpServletRequest request, HttpServletResponse response) {
      WoopraTracker woopra = new WoopraTrackerEE(request, response);
      woopra.config(WoopraTracker.DOMAIN, "mybusiness.com").identify(user).track();
      // Create an event
      myEvent = new WoopraEvent("play");
      myEvent.setProperty("artist", "Dave Brubeck");
      myEvent.setProperty("song", "Take Five");
      myEvent.setProperty("genre", "Jazz");
      woopra.track(myEvent);
      // Track it through the front end...
      woopra.track(myEvent)
      // ... or through the back end by passing the optional param True
      woopra.track(myEvent, true)

      //Your code here...

      woopra.setWoopraCookie();
      request.setAttribute("woopraCode", woopra.woopraCode());
      this.getServletContext().getRequestDispatcher("/WEB-INF/homepage.jsp").forward(request, response);
```
and print the woopraCode in your jsp's header:
``` jsp
<head>
   ...
   <%= request.getAttribute("woopraCode") %>
</head>
```
If you prefer, you can also track an event without even having to create a WoopraEvent Object:
``` java
woopra.track("play", new Object[][] {
   {"artist", "Dave Brubeck"},
   {"song", "Take Five"},
   {"genre", "Jazz"},
}, true);
```
