
package com.flippingutilities.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

/**
 * This class represents a flip to be made in the history panel.
 */
@Data
@AllArgsConstructor
public class Flip
{
	int buyPrice;
	int sellPrice;
	int quantity;
	Instant time;
	boolean marginCheck;
	boolean ongoing;
}
