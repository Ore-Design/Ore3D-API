package design.ore.Ore3DAPI.Extensions;

import org.pf4j.ExtensionPoint;

import design.ore.Ore3DAPI.Records.Customer;
import design.ore.Ore3DAPI.Records.Employee;
import design.ore.Ore3DAPI.Records.Transaction;

public interface CRMEndpoint extends ExtensionPoint
{
	/*
	 * Retrieves the <code>Transaction</code> stored under a given <code>id</code>.
	 * 
	 * @param   id   a unique id to be handled by the CRM as a location for the <code>Transaction</code>
	 * @return       the Transaction found at the provided location, or null if not found.
	 * @see          Transaction
	 */
	Transaction getTransaction(String id);
	
	/*
	 * Updates the <code>Transaction</code> associated with the id, to be handled by the CRM.
	 * 
	 * @param   id            a unique id to be handled by the CRM as a location for the <code>Transaction</code>
	 * @param   transaction   the <code>Transaction</code> to be updated.
	 * @return                true if successful, otherwise false.
	 * @see                   Transaction
	 */
	boolean updateTransaction(String id, Transaction transaction);
	
	/*
	 * Adds a new <code>Transaction</code> to the CRM.
	 * 
	 * @param   transaction   the <code>Transaction</code> to be updated.
	 * @return                the id of the newly created <code>Transaction</code>.
	 * @see                   Transaction
	 */
	String addTransaction(Transaction transaction);

	/*
	 * Retrieves the <code>Customer</code> stored under a given <code>id</code>.
	 * 
	 * @param   id   a unique id to be handled by the CRM as a location for the <code>Customer</code>
	 * @return       the Customer found at the provided location, or null if not found.
	 * @see          Customer
	 */
	Customer getCustomer(String id);
	
	/*
	 * Updates the <code>Customer</code> associated with the id, to be handled by the CRM.
	 * 
	 * @param   id          a unique id to be handled by the CRM as a location for the <code>Customer</code>
	 * @param   customer    the <code>Customer</code> to be updated.
	 * @return              true if successful, otherwise false.
	 * @see                 Customer
	 */
	boolean updateCustomer(String id, Customer transaction);
	
	/*
	 * Adds a new <code>Customer</code> to the CRM.
	 * 
	 * @param   customer    the <code>Customer</code> to be updated.
	 * @return              the id of the newly created <code>Customer</code>.
	 * @see                 Transaction
	 */
	String addCustomer(Customer customer);

	/*
	 * Retrieves the <code>Employee</code> stored under a given <code>id</code>.
	 * 
	 * @param   id   a unique id to be handled by the CRM as a location for the <code>Employee</code>
	 * @return       the Employee found at the provided location, or null if not found.
	 * @see          Employee
	 */
	Customer getEmployee(String id);
	
	/*
	 * Updates the <code>Employee</code> associated with the id, to be handled by the CRM.
	 * 
	 * @param   id           a unique id to be handled by the CRM as a location for the <code>Employee</code>
	 * @param   employee     the <code>Employee</code> to be updated.
	 * @return               true if successful, otherwise false.
	 * @see                  Employee
	 */
	boolean updateEmployee(String id, Employee employee);
	
	/*
	 * Adds a new <code>Employee</code> to the CRM.
	 * 
	 * @param   employee    the <code>Employee</code> to be updated.
	 * @return              the id of the newly created <code>Employee</code>.
	 * @see                 Employee
	 */
	String addEmployee(Employee employee);
}