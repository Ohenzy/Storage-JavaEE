package com.ohenzy.warehouse.storage;

import com.ohenzy.warehouse.models.Product;
import com.ohenzy.warehouse.storage.settings.Connector;

import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class ProductStorage  {




    private final Connector connector = Connector.getInstance();

    public ProductStorage(){
        this.createTable();
    }

    public void saveAll(List<Product> products){
        for (Product product : products)
            try(PreparedStatement statement = connector.getConnection().prepareStatement("select * from products where name = (?) and unit = (?);")) {
                statement.setString(1,product.getName());
                statement.setString(2,product.getUnit());
                ResultSet result = statement.executeQuery();
                if (result.next()){
                    int quantity = result.getInt("quantity") + product.getQuantity();
                    if (quantity > 0)
                        statement.execute("update products set quantity = " + quantity +" where name ='"+ product.getName() +"' and unit = '" + product.getUnit() +"';");
                    else if (quantity == 0)
                        statement.execute("delete from products where product_id = " + result.getInt("product_id"));
                } else
                    statement.execute("insert into products (name, quantity, unit) values ('" + product.getName() + "', " + product.getQuantity() + ", '" + product.getUnit() +"');");
            } catch (SQLException e){
                e.printStackTrace();
            }
    }

    public boolean deleteById(String deleteId){
        if(!deleteId.equals(""))
            if(existsById(Integer.parseInt(deleteId))){
                try (PreparedStatement statement = connector.getConnection().prepareStatement("delete from products where product_id=(?)")){
                    statement.setInt(1, Integer.parseInt(deleteId));
                    statement.executeUpdate();
                    return true;
                } catch (SQLException e){
                    e.printStackTrace();
                    return false;
                }
            }
        return false;
    }

    public List<Product> findAll(){
        final List<Product> units = new ArrayList<>();
        try(ResultSet result  = connector.getConnection().createStatement().executeQuery("select * from products")){
            while (result.next())
                units.add(new Product(result.getInt("product_id"),result.getString("name"), result.getInt("quantity"),result.getString("unit")));
        }catch (SQLException e){
            e.printStackTrace();
        }
        return units;
    }

    public boolean existsById(int id) {
        boolean exists = false;
        try (PreparedStatement statement = connector.getConnection().prepareStatement("select * from products where product_id=(?)")){
            statement.setInt(1, id);
            exists = statement.executeQuery().next();
        } catch (SQLException e){
            e.printStackTrace();
        }
        return exists;
    }

    private void createTable() {
        try {
            if( !tableExists() )
                connector.getConnection().createStatement().executeUpdate("create table products ( product_id int not null auto_increment primary key, name varchar (50), quantity int not null, unit varchar (50));");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean tableExists() throws SQLException{
        boolean tableExists = false;
        ResultSet result = connector.getConnection().createStatement().executeQuery("CHECK TABLE products");
        while (result.next())
            if (result.getString("Msg_text").equals("OK"))
                tableExists = true;

        return tableExists;
    }

}
