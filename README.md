![Order Logo](order.png)

Örder is an innovative (not really) order management application. The front-end 
will be a beautiful designed javascript-based application that will communicate with an even more 
beautiful engineered back-end application.

It's your job to implement this back end application! Good luck and, more importantly, have Fun!

- Please don't use the `Ö` in the naming of your files, directories or packages...
    - Instead, use **eurder**.
    - ️⚠️ WARNING: don't use the word "order", it is a reserved keyword in many contexts (e.g. in PostgreSQL) ⚠️
      - For your information, here is [the full list of reserved keywords for PostgreSQL](https://www.postgresql.org/docs/current/sql-keywords-appendix.html)

## Timing

To **properly** implement all the stories, in a **test-first way** with a **proper design** and **architecture**, 
an experienced developer will need around 2-3 days, it might be more, it might be less. Therefore, we've created two categories 
of stories: **Must-Have's** and **Nice-To-Have's**. Start with the Must-Have's, then do the Nice-To-Have's.

**Must-Have stories:**
- Story 0 (your project setup + other technical requirements)
- Story 1
- Story 2
- Story 3
- Story 7
- Story 8

**Nice-To-Have stories:**
- Story 4
- Story 5
- Story 6
- Story 9
- Story 10

## Technical requirements for JAVA

- Create a new GitHub repository
- Create a REST(ful) Web API (with JSON as the message / payload format)
- Use Spring Boot or Quarkus (latest (LTS) version)
- Use Maven as the Build & Dependency Management project tool.
- Perform logging
    - Certainly log all interactions with the application that can be defined as "errors"
        - E.g.: unauthorized access, illegal arguments, exceptions in general,...
- Provide, through OpenAPI and Swagger(UI) an online manual / documentation for your Web API.
- **If you have already seen JPA**: use JPA (Hibernate or EclipseLink) in combination with a PostgreSQL or Oracle Database to store and access the data.
    - Setup a proper test configuration, which runs the integration tests against an in-memory database (e.g. H2)
        - Make it a separate technical story.
    - Correctly setup and handle the transactions.
    - Write your DDL (create tables,...) in a separate `.sql` file, which you also put under version control.
- Use Jenkins to set up a Continuous Integration (CI) pipeline.
    - Additionally, but optional, deploy to Heroku!
- Think about Security: authentication and authorization. It is not a priority, but if you have the time, implement it properly.
    - Until then, you can neglect the fact that certain endpoints should only be usable by - for example - an administrator.

## Technical requirements for .NET

- Create a new GitHub repository 
- Use REST (with JSON as the message / body format)
- Use ASP.NET Core Web Api
- Use AzureDevops for continuous integration
- Perform logging (use logging provided by .NET Core)
    - Certainly log all interactions with the application that can be defined as "errors"
            - E.g.: unauthorized access, illegal arguments, exceptions in general,...
- Use Swagger to provide a readable document of your WebApi
- Think about Security: authentication and authorization. It is not a priority, but if you have the time, implement it properly.
    - Until then, you can neglect the fact that certain endpoints should only be usable by - for example - an administrator.

## Functional stories
The functional requirements are written down as stories. 

### Story 1: Create a customer account
As an unregistered user I want to create a customer account so I can become recognized within the system.
- Upon creation of a customer, the following data should be provided:
    - First name
    - Last name
    - Email address
    - Address
    - Phone number

### Story 2: Add an item
As an admin user I want to add an item so I can expand the list of available items.
- When adding an `Item`, the following data should be provided
    - Name
    - Description
    - Price
    - Amount (stock)

### Story 3: Order items
As a customer I want to order one or more items.
- An `Order` contains one or more **item groups**
- An `item group` contains a selected item (id), an amount, and a shipping date. 
    - The shipping date should be calculated automatically:
        - When we have the item in stock, the shipping date is set to the next day (of the order)
        - Otherwise the shipping date should be set to next week (day of order + 7 days)
- The total price should be calculated and shown to the customer when ordering.
- Question: should an order keep a reference to an item or should it make some sort of copy?
    - Tip: The the price of the item can change over time... What implications might this have?
- Obviously, we also need to keep track of who made the order (it has to be a known customer)

### Story 4: Update an item
As an admin user I want to update an item so I can keep my list of available items up to date.
- When updating an Item, the following data should be provided
    - Name
    - Description
    - Price
    - Amount (stock)

### Story 5: View report of orders
As a customer I want to see a report of all my orders so I can get an overview of what I ordered and how much it cost.
- The report should contain per order:
    - The id(entifier) of the order
    - Per item group of the order
        - The name of the item
        - The ordered amount 
        - The total price of the item group
    - The total price of the order
- The total price of all orders

### Story 6: Reorder an existing order 
As a customer I want to be able to reorder an existing order so I can quickly place a recurring order.
- Make sure this can be done by providing an order id(entifier).
- A customer can only reorder one of his own orders.
- The actual price of the item should be used, not the price the item had in the existing order.

### Story 7: View all customers
As an admin user I want to view all customers.
- List all customers known by the system

### Story 8: View a single customer
As an admin user I want to view the details of a single customers.
- Based on the customer identifier

### Story 9: Items Shipping today
As an admin user I want to view all orders that contain items that should be shipped today.
- List the item groups that should be shipped today.
- Give the address where they should be shipped to. 
- Note: item groups of the same order can have a different shipping date, 
but all items are shipped to the same address.

### Story 10: Item overview
As an admin user I want to have an overview of items and their stock resupply urgency so I can resupply items in a timely matter.
- To indicate the urgency of stock resupply we need to use a very business specific urgency indicator.
- There are 3 different levels for the urgency indicator:
    - STOCK_LOW
        - If item amount < 5
    - STOCK_MEDIUM
        - If item amount < 10
    - STOCK_HIGH
        -  If item amount >= 10
- The list of items should be ordered based on stock resupply urgency. Starting from most urgent to less urgent.
- I should be able to provide a filter, selecting only the items that have a certain level.
    - E.g.: Give me the list of items with urgency indicator STOCK_LOW 

## Additional extensions

> Completely optional! Only for those that are really ahead and fully finished with all the previous requirements of Örder.

A new feature request was made by the business of Örder.
For every Item, they would like to be able to set a state (pristine, slightly damaged or damaged).
- A slightly damaged item will receive a 5% discount when ordered. 
- A damaged item will receive a 10% discount when ordered.

This feature might impact many previous stories... You will have to change a lot of your code.
- Until now, we had (for example) an Item "Fridge Coolio Z" which has Amount 5 (thus 5 fridges)
    - You will now have to provide a different modeling. Each "Fridge Coolio Z" Item will represent a single fridge.
    
Analyse which stories are impacted by this change, analyse how you will redesign Item (hint: do you need an additional class?)
- Ask questions when something is not clear. 
    
## Story map

![Story map](Story_map_order.png)

# Self evaluation 4: Örder

As the self evaluation, we'll re-develop the Örder application. But this time, we'll focus on the ORM part (and thus the domain).

- The actual assignment can be found in `60-projects/order`
- Start by creating your entire domain and properly implementing the ORM.
    - When you're finished with the domain and still have time left, implement the REST endpoints (and services,...)
- As your starting point, you can use a skeleton that's available on the Switchfully GitHub.
- Since this is a self evaluation exercise, work individually.


