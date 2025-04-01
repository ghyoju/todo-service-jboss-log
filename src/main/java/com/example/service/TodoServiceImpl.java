package com.example.service;

import com.example.entity.Todo;
import com.example.todoservice.TodoService;
import jakarta.jws.WebService;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebService(
    serviceName = "TodoService",
    portName = "TodoPort",
    targetNamespace = "http://todoservice.example.com/",
    endpointInterface = "com.example.todoservice.TodoService",
    wsdlLocation = "WEB-INF/wsdl/TodoService.wsdl"
)
public class TodoServiceImpl implements TodoService {

    private static final Logger logger = LoggerFactory.getLogger(TodoServiceImpl.class);
    private static final SessionFactory sessionFactory;

    static {
        try {
            ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
            sessionFactory = context.getBean("sessionFactory", SessionFactory.class);
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Failed to initialize SessionFactory: " + e.getMessage());
        }
    }

    @Override
    public String addTodo(String task) {
        Session session = null;
        logger.info("Processing request.addTodo..");
        logger.debug("Debugging information.addTodo.");
        
        try {
            session = sessionFactory.openSession();
            session.beginTransaction();

            Todo todo = new Todo();
            todo.setTask(task);
            todo.setCompleted(false);
            session.save(todo);

            session.getTransaction().commit();

            logger.info("Processing request.addTodo.. DONE");
            logger.debug("Debugging information.addTodo. DONE");

            return "Todo added with ID: " + todo.getId();
        } catch (Exception e) {
            logger.error("An error occurred!", e.getMessage());

            if (session != null && session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to add todo: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public Todo getTodo(Long id) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            session.beginTransaction();

            String sql = "SELECT * FROM Todo WHERE id = :id";
            Todo todo = (Todo) session.createNativeQuery(sql, Todo.class)
                                      .setParameter("id", id)
                                      .uniqueResult();

            session.getTransaction().commit();
            return todo;
        } catch (Exception e) {
            if (session != null && session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to get todo: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}