


package uk.gov.dwp.uc.pairtest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;

import static org.mockito.Mockito.*;

public class TestTicketServiceImpl {

    private TicketPaymentService paymentService;
    private SeatReservationService seatReservationService;
    private TicketServiceImpl ticketService;

    @Before
    public void setUp() {
        // Mock the dependencies
        paymentService = mock(TicketPaymentService.class);
        seatReservationService = mock(SeatReservationService.class);
        ticketService = new TicketServiceImpl(paymentService, seatReservationService);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testInvalidAccountId() throws InvalidPurchaseException {
        // Test with invalid account ID
        ticketService.purchaseTickets(-1L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1));
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testMoreThan25Tickets() throws InvalidPurchaseException {
        // Test with more than 25 tickets
        ticketService.purchaseTickets(1L, 
            new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 20), 
            new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 6));
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testChildWithoutAdult() throws InvalidPurchaseException {
        // Test with child tickets but no adult ticket
        ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1));
    }

    @Test
    public void testValidPurchase() throws InvalidPurchaseException {
        // Test a valid ticket purchase
        ticketService.purchaseTickets(1L, 
            new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2), 
            new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1));

        // Verify the interactions with mocks
        verify(paymentService).makePayment(1L, 65); // 2 adults * 25 + 1 child * 15 = 65
        verify(seatReservationService).reserveSeat(1L, 3); // 2 adults + 1 child
    }
}

