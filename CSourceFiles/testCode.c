#include <avr/io.h>
#include <util/delay.h>
int main(void) {

	//Setting the baud rate to 2400
	UCSRC &= ~(1 << UMSEL);
	UBBRH &= ~(1 << URSEL);
	UBBRH = (unsigned char) (520 >> 8);
	UBBRL = (unsigned char) 520;
	//Enable the receiver and transmitter
	UCSRB = (1 << RXEN) | (1 << TXEN);

	//Set 2 stop bits and data bit length is 8-bit
	UCSRC = (1 << URSEL) | (1 << USBS) | (3 << UCSZ0);

	//Set the first 5 pins of port b to be outputs
	DDRB = 0b00011111;
	//PORTB = 0b00000001;

	//Set pin 0 of port D to be inputs, pins 1-5 are outputs
	DDRD = 0b00011110;
	while(1) {

	}

	unsigned char USART_Receive( void )
	{
		while (! (UCSR0A & (1 << RXC0)) );

		receiveData = UDR0;

		if (receiveData == 0b11110000) PORTB ^= (1 << PINB0);
	}


	void USART_Transmit (unsigned int data)
	{
		//Wait until the Transmitter is ready
		while (! (UCSRA & (1 << UDRE)) ); 

		//Make the 9th bit 0 for the moment
		UCSRB &=~(1 << TXB8); 

		//If the 9th bit of the data is a 1
		if (data & 0x0100) 

		//Set the TXB8 bit to 1
		USCRB |= (1 << TXB8); 

		//Get that data outa here!
		UDR = data;
	}


	
}