package org.example;

import org.example.model.Todo;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class DatabaseHelper {
    private SessionFactory sessionFactory;

    public DatabaseHelper() {
        Configuration configuration = new Configuration()
                .addAnnotatedClass(Todo.class)
                .setProperty("hibernate.connection.driver_class", "org.sqlite.JDBC")
                .setProperty("hibernate.connection.url", "jdbc:sqlite:mydb.db")
                .setProperty("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect")
                .setProperty("hibernate.show_sql", "true")
                .setProperty("hibernate.hdm2ddl.auto", "update")
                .setProperty("hibernate.format_sql", "true");

        sessionFactory = configuration.buildSessionFactory();
    }

    public void saveTodo(Todo todo){
        Session session = sessionFactory.openSession();
        session.getTransaction().begin();
        session.merge(todo);
        session.getTransaction().commit();
        session.close();
    }

    public void deleteTodoById(String id){
        Session session = sessionFactory.openSession();
        session.getTransaction().begin();
        Todo todo = session.get(Todo.class, id);
        session.remove(todo);
        session.getTransaction().commit();
        session.close();
    }

    public void updateTodoById(String id, Todo todo){
        Session session = sessionFactory.openSession();
        session.getTransaction().begin();
        Todo todoFromDB = session.get(Todo.class, id);
        todoFromDB.setCompleted(todo.getCompleted());
        session.getTransaction().commit();
        session.close();
    }

    public Todo[] getTodos(){
        Session session = sessionFactory.openSession();
        session.getTransaction().begin();
        Todo[] todoList = session.createQuery("SELECT t FROM Todo t", Todo.class).getResultList().toArray(new Todo[0]);
        session.close();
        return todoList;
    }


}
