  public void initialize(Map<String, Object> args) {
  }

  public Object predict(Map<String, Object> inputs) {
		String income = (String)nameToValue.get("income");

		String education = (String)nameToValue.get("education");

		String marital_status = (String)nameToValue.get("marital_status");

		String occupation = (String)nameToValue.get("occupation");

		String native_country = (String)nameToValue.get("native_country");

		Integer age = (Integer)nameToValue.get("age");
		Integer education_num = (Integer)nameToValue.get("education_num");
		Integer capital_gain = (Integer)nameToValue.get("capital_gain");
		Integer capital_loss = (Integer)nameToValue.get("capital_loss");
		Integer hours_per_week = (Integer)nameToValue.get("hours_per_week");
		income = "<=50K";
		Boolean __succ0 = false;
		if (!__succ0) {
			Integer __predicateValue1 = marital_status == null? 3 : (marital_status.equals("Married-civ-spouse") ? 2 : 1);
			if (__predicateValue1 == 1) {
				__succ0 = true;
				income = "<=50K";
				Boolean __succ2 = false;
				if (!__succ2) {
					Integer __predicateValue3 = capital_gain == null? 3 : ((capital_gain <= 7688)? 1 : 2);
					if (__predicateValue3 == 1) {
						__succ2 = true;
						income = "<=50K";
						Boolean __succ4 = false;
						if (!__succ4) {
							Integer __predicateValue5 = education_num == null? 3 : ((education_num <= 13)? 1 : 2);
							if (__predicateValue5 == 1) {
								__succ4 = true;
								income = "<=50K";
								Boolean __succ6 = false;
								if (!__succ6) {
									Integer __predicateValue7 = hours_per_week == null? 3 : ((hours_per_week <= 42)? 1 : 2);
									if (__predicateValue7 == 1) {
										__succ6 = true;
										income = "<=50K";
										Boolean __succ8 = false;
										if (!__succ8) {
											Integer __predicateValue9 = age == null? 3 : ((age <= 33)? 1 : 2);
											if (__predicateValue9 == 1) {
												__succ8 = true;
												income = "<=50K";
											}
											else if (__predicateValue9 == 3) {
											}
										}
										if (!__succ8) {
											Integer __predicateValue10 = age == null? 3 : ((age > 33)? 1 : 2);
											if (__predicateValue10 == 1) {
												__succ8 = true;
												income = "<=50K";
											}
											else if (__predicateValue10 == 3) {
											}
										}
										if (!__succ8) {
											income = null;
											resultExplanation = null;
										}
									}
									else if (__predicateValue7 == 3) {
									}
								}
								if (!__succ6) {
									Integer __predicateValue11 = hours_per_week == null? 3 : ((hours_per_week > 42)? 1 : 2);
									if (__predicateValue11 == 1) {
										__succ6 = true;
										income = "<=50K";
										Boolean __succ12 = false;
										if (!__succ12) {
											Integer __predicateValue13 = education == null? 3 : (education.equals("Bachelors") ? 2 : 1);
											if (__predicateValue13 == 1) {
												__succ12 = true;
												income = "<=50K";
											}
											else if (__predicateValue13 == 3) {
											}
										}
										if (!__succ12) {
											Integer __predicateValue14 = education == null? 3 : (education.equals("Bachelors") ? 1 : 2);
											if (__predicateValue14 == 1) {
												__succ12 = true;
												income = "<=50K";
											}
											else if (__predicateValue14 == 3) {
											}
										}
										if (!__succ12) {
											income = null;
											resultExplanation = null;
										}
									}
									else if (__predicateValue11 == 3) {
									}
								}
								if (!__succ6) {
									income = null;
									resultExplanation = null;
								}
							}
							else if (__predicateValue5 == 3) {
							}
						}
						if (!__succ4) {
							Integer __predicateValue15 = education_num == null? 3 : ((education_num > 13)? 1 : 2);
							if (__predicateValue15 == 1) {
								__succ4 = true;
								income = "<=50K";
								Boolean __succ16 = false;
								if (!__succ16) {
									Integer __predicateValue17 = hours_per_week == null? 3 : ((hours_per_week <= 43)? 1 : 2);
									if (__predicateValue17 == 1) {
										__succ16 = true;
										income = "<=50K";
										Boolean __succ18 = false;
										if (!__succ18) {
											Integer __predicateValue19 = age == null? 3 : ((age <= 32)? 1 : 2);
											if (__predicateValue19 == 1) {
												__succ18 = true;
												income = "<=50K";
											}
											else if (__predicateValue19 == 3) {
											}
										}
										if (!__succ18) {
											Integer __predicateValue20 = age == null? 3 : ((age > 32)? 1 : 2);
											if (__predicateValue20 == 1) {
												__succ18 = true;
												income = "<=50K";
											}
											else if (__predicateValue20 == 3) {
											}
										}
										if (!__succ18) {
											income = null;
											resultExplanation = null;
										}
									}
									else if (__predicateValue17 == 3) {
									}
								}
								if (!__succ16) {
									Integer __predicateValue21 = hours_per_week == null? 3 : ((hours_per_week > 43)? 1 : 2);
									if (__predicateValue21 == 1) {
										__succ16 = true;
										income = "<=50K";
										Boolean __succ22 = false;
										if (!__succ22) {
											Integer __predicateValue23 = age == null? 3 : ((age <= 32)? 1 : 2);
											if (__predicateValue23 == 1) {
												__succ22 = true;
												income = "<=50K";
											}
											else if (__predicateValue23 == 3) {
											}
										}
										if (!__succ22) {
											Integer __predicateValue24 = age == null? 3 : ((age > 32)? 1 : 2);
											if (__predicateValue24 == 1) {
												__succ22 = true;
												income = "<=50K";
											}
											else if (__predicateValue24 == 3) {
											}
										}
										if (!__succ22) {
											income = null;
											resultExplanation = null;
										}
									}
									else if (__predicateValue21 == 3) {
									}
								}
								if (!__succ16) {
									income = null;
									resultExplanation = null;
								}
							}
							else if (__predicateValue15 == 3) {
							}
						}
						if (!__succ4) {
							income = null;
							resultExplanation = null;
						}
					}
					else if (__predicateValue3 == 3) {
					}
				}
				if (!__succ2) {
					Integer __predicateValue25 = capital_gain == null? 3 : ((capital_gain > 7688)? 1 : 2);
					if (__predicateValue25 == 1) {
						__succ2 = true;
						income = ">50K";
						Boolean __succ26 = false;
						if (!__succ26) {
							Integer __predicateValue27 = age == null? 3 : ((age <= 20)? 1 : 2);
							if (__predicateValue27 == 1) {
								__succ26 = true;
								income = "<=50K";
								Boolean __succ28 = false;
								if (!__succ28) {
									Integer __predicateValue29 = education == null? 3 : (education.equals("Some-college") ? 2 : 1);
									if (__predicateValue29 == 1) {
										__succ28 = true;
										income = "<=50K";
									}
									else if (__predicateValue29 == 3) {
									}
								}
								if (!__succ28) {
									Integer __predicateValue30 = education == null? 3 : (education.equals("Some-college") ? 1 : 2);
									if (__predicateValue30 == 1) {
										__succ28 = true;
										income = ">50K";
									}
									else if (__predicateValue30 == 3) {
									}
								}
								if (!__succ28) {
									income = null;
									resultExplanation = null;
								}
							}
							else if (__predicateValue27 == 3) {
							}
						}
						if (!__succ26) {
							Integer __predicateValue31 = age == null? 3 : ((age > 20)? 1 : 2);
							if (__predicateValue31 == 1) {
								__succ26 = true;
								income = ">50K";
								Boolean __succ32 = false;
								if (!__succ32) {
									Integer __predicateValue33 = occupation == null? 3 : (occupation.equals("Protective-serv") ? 1 : 2);
									if (__predicateValue33 == 1) {
										__succ32 = true;
										income = ">50K";
										Boolean __succ34 = false;
										if (!__succ34) {
											Integer __predicateValue35 = age == null? 3 : ((age <= 36)? 1 : 2);
											if (__predicateValue35 == 1) {
												__succ34 = true;
												income = ">50K";
											}
											else if (__predicateValue35 == 3) {
											}
										}
										if (!__succ34) {
											Integer __predicateValue36 = age == null? 3 : ((age > 36)? 1 : 2);
											if (__predicateValue36 == 1) {
												__succ34 = true;
												income = "<=50K";
											}
											else if (__predicateValue36 == 3) {
											}
										}
										if (!__succ34) {
											income = null;
											resultExplanation = null;
										}
									}
									else if (__predicateValue33 == 3) {
									}
								}
								if (!__succ32) {
									Integer __predicateValue37 = occupation == null? 3 : (occupation.equals("Protective-serv") ? 2 : 1);
									if (__predicateValue37 == 1) {
										__succ32 = true;
										income = ">50K";
										Boolean __succ38 = false;
										if (!__succ38) {
											Integer __predicateValue39 = age == null? 3 : ((age <= 54)? 1 : 2);
											if (__predicateValue39 == 1) {
												__succ38 = true;
												income = ">50K";
											}
											else if (__predicateValue39 == 3) {
											}
										}
										if (!__succ38) {
											Integer __predicateValue40 = age == null? 3 : ((age > 54)? 1 : 2);
											if (__predicateValue40 == 1) {
												__succ38 = true;
												income = ">50K";
											}
											else if (__predicateValue40 == 3) {
											}
										}
										if (!__succ38) {
											income = null;
											resultExplanation = null;
										}
									}
									else if (__predicateValue37 == 3) {
									}
								}
								if (!__succ32) {
									income = null;
									resultExplanation = null;
								}
							}
							else if (__predicateValue31 == 3) {
							}
						}
						if (!__succ26) {
							income = null;
							resultExplanation = null;
						}
					}
					else if (__predicateValue25 == 3) {
					}
				}
				if (!__succ2) {
					income = null;
					resultExplanation = null;
				}
			}
			else if (__predicateValue1 == 3) {
			}
		}
		if (!__succ0) {
			Integer __predicateValue41 = marital_status == null? 3 : (marital_status.equals("Married-civ-spouse") ? 1 : 2);
			if (__predicateValue41 == 1) {
				__succ0 = true;
				income = "<=50K";
				Boolean __succ42 = false;
				if (!__succ42) {
					Integer __predicateValue43 = education_num == null? 3 : ((education_num <= 12)? 1 : 2);
					if (__predicateValue43 == 1) {
						__succ42 = true;
						income = "<=50K";
						Boolean __succ44 = false;
						if (!__succ44) {
							Integer __predicateValue45 = capital_gain == null? 3 : ((capital_gain <= 3887)? 1 : 2);
							if (__predicateValue45 == 1) {
								__succ44 = true;
								income = "<=50K";
								Boolean __succ46 = false;
								if (!__succ46) {
									Integer __predicateValue47 = education_num == null? 3 : ((education_num <= 8)? 1 : 2);
									if (__predicateValue47 == 1) {
										__succ46 = true;
										income = "<=50K";
										Boolean __succ48 = false;
										if (!__succ48) {
											Integer __predicateValue49 = hours_per_week == null? 3 : ((hours_per_week <= 43)? 1 : 2);
											if (__predicateValue49 == 1) {
												__succ48 = true;
												income = "<=50K";
											}
											else if (__predicateValue49 == 3) {
											}
										}
										if (!__succ48) {
											Integer __predicateValue50 = hours_per_week == null? 3 : ((hours_per_week > 43)? 1 : 2);
											if (__predicateValue50 == 1) {
												__succ48 = true;
												income = "<=50K";
											}
											else if (__predicateValue50 == 3) {
											}
										}
										if (!__succ48) {
											income = null;
											resultExplanation = null;
										}
									}
									else if (__predicateValue47 == 3) {
									}
								}
								if (!__succ46) {
									Integer __predicateValue51 = education_num == null? 3 : ((education_num > 8)? 1 : 2);
									if (__predicateValue51 == 1) {
										__succ46 = true;
										income = "<=50K";
										Boolean __succ52 = false;
										if (!__succ52) {
											Integer __predicateValue53 = capital_loss == null? 3 : ((capital_loss <= 1816)? 1 : 2);
											if (__predicateValue53 == 1) {
												__succ52 = true;
												income = "<=50K";
											}
											else if (__predicateValue53 == 3) {
											}
										}
										if (!__succ52) {
											Integer __predicateValue54 = capital_loss == null? 3 : ((capital_loss > 1816)? 1 : 2);
											if (__predicateValue54 == 1) {
												__succ52 = true;
												income = ">50K";
											}
											else if (__predicateValue54 == 3) {
											}
										}
										if (!__succ52) {
											income = null;
											resultExplanation = null;
										}
									}
									else if (__predicateValue51 == 3) {
									}
								}
								if (!__succ46) {
									income = null;
									resultExplanation = null;
								}
							}
							else if (__predicateValue45 == 3) {
							}
						}
						if (!__succ44) {
							Integer __predicateValue55 = capital_gain == null? 3 : ((capital_gain > 3887)? 1 : 2);
							if (__predicateValue55 == 1) {
								__succ44 = true;
								income = ">50K";
								Boolean __succ56 = false;
								if (!__succ56) {
									Integer __predicateValue57 = capital_gain == null? 3 : ((capital_gain <= 7688)? 1 : 2);
									if (__predicateValue57 == 1) {
										__succ56 = true;
										income = ">50K";
										Boolean __succ58 = false;
										if (!__succ58) {
											Integer __predicateValue59 = hours_per_week == null? 3 : ((hours_per_week <= 29)? 1 : 2);
											if (__predicateValue59 == 1) {
												__succ58 = true;
												income = "<=50K";
											}
											else if (__predicateValue59 == 3) {
											}
										}
										if (!__succ58) {
											Integer __predicateValue60 = hours_per_week == null? 3 : ((hours_per_week > 29)? 1 : 2);
											if (__predicateValue60 == 1) {
												__succ58 = true;
												income = ">50K";
											}
											else if (__predicateValue60 == 3) {
											}
										}
										if (!__succ58) {
											income = null;
											resultExplanation = null;
										}
									}
									else if (__predicateValue57 == 3) {
									}
								}
								if (!__succ56) {
									Integer __predicateValue61 = capital_gain == null? 3 : ((capital_gain > 7688)? 1 : 2);
									if (__predicateValue61 == 1) {
										__succ56 = true;
										income = ">50K";
										Boolean __succ62 = false;
										if (!__succ62) {
											Integer __predicateValue63 = education_num == null? 3 : ((education_num <= 4)? 1 : 2);
											if (__predicateValue63 == 1) {
												__succ62 = true;
												income = "<=50K";
											}
											else if (__predicateValue63 == 3) {
											}
										}
										if (!__succ62) {
											Integer __predicateValue64 = education_num == null? 3 : ((education_num > 4)? 1 : 2);
											if (__predicateValue64 == 1) {
												__succ62 = true;
												income = ">50K";
											}
											else if (__predicateValue64 == 3) {
											}
										}
										if (!__succ62) {
											income = null;
											resultExplanation = null;
										}
									}
									else if (__predicateValue61 == 3) {
									}
								}
								if (!__succ56) {
									income = null;
									resultExplanation = null;
								}
							}
							else if (__predicateValue55 == 3) {
							}
						}
						if (!__succ44) {
							income = null;
							resultExplanation = null;
						}
					}
					else if (__predicateValue43 == 3) {
					}
				}
				if (!__succ42) {
					Integer __predicateValue65 = education_num == null? 3 : ((education_num > 12)? 1 : 2);
					if (__predicateValue65 == 1) {
						__succ42 = true;
						income = ">50K";
						Boolean __succ66 = false;
						if (!__succ66) {
							Integer __predicateValue67 = capital_gain == null? 3 : ((capital_gain <= 3887)? 1 : 2);
							if (__predicateValue67 == 1) {
								__succ66 = true;
								income = ">50K";
								Boolean __succ68 = false;
								if (!__succ68) {
									Integer __predicateValue69 = capital_loss == null? 3 : ((capital_loss <= 1816)? 1 : 2);
									if (__predicateValue69 == 1) {
										__succ68 = true;
										income = ">50K";
										Boolean __succ70 = false;
										if (!__succ70) {
											Integer __predicateValue71 = hours_per_week == null? 3 : ((hours_per_week <= 34)? 1 : 2);
											if (__predicateValue71 == 1) {
												__succ70 = true;
												income = "<=50K";
											}
											else if (__predicateValue71 == 3) {
											}
										}
										if (!__succ70) {
											Integer __predicateValue72 = hours_per_week == null? 3 : ((hours_per_week > 34)? 1 : 2);
											if (__predicateValue72 == 1) {
												__succ70 = true;
												income = ">50K";
											}
											else if (__predicateValue72 == 3) {
											}
										}
										if (!__succ70) {
											income = null;
											resultExplanation = null;
										}
									}
									else if (__predicateValue69 == 3) {
									}
								}
								if (!__succ68) {
									Integer __predicateValue73 = capital_loss == null? 3 : ((capital_loss > 1816)? 1 : 2);
									if (__predicateValue73 == 1) {
										__succ68 = true;
										income = ">50K";
										Boolean __succ74 = false;
										if (!__succ74) {
											Integer __predicateValue75 = native_country == null? 3 : (native_country.equals("Iran") ? 1 : 2);
											if (__predicateValue75 == 1) {
												__succ74 = true;
												income = "<=50K";
											}
											else if (__predicateValue75 == 3) {
											}
										}
										if (!__succ74) {
											Integer __predicateValue76 = native_country == null? 3 : (native_country.equals("Iran") ? 2 : 1);
											if (__predicateValue76 == 1) {
												__succ74 = true;
												income = ">50K";
											}
											else if (__predicateValue76 == 3) {
											}
										}
										if (!__succ74) {
											income = null;
											resultExplanation = null;
										}
									}
									else if (__predicateValue73 == 3) {
									}
								}
								if (!__succ68) {
									income = null;
									resultExplanation = null;
								}
							}
							else if (__predicateValue67 == 3) {
							}
						}
						if (!__succ66) {
							Integer __predicateValue77 = capital_gain == null? 3 : ((capital_gain > 3887)? 1 : 2);
							if (__predicateValue77 == 1) {
								__succ66 = true;
								income = ">50K";
								Boolean __succ78 = false;
								if (!__succ78) {
									Integer __predicateValue79 = age == null? 3 : ((age <= 26)? 1 : 2);
									if (__predicateValue79 == 1) {
										__succ78 = true;
										income = "<=50K";
										Boolean __succ80 = false;
										if (!__succ80) {
											Integer __predicateValue81 = hours_per_week == null? 3 : ((hours_per_week <= 40)? 1 : 2);
											if (__predicateValue81 == 1) {
												__succ80 = true;
												income = "<=50K";
											}
											else if (__predicateValue81 == 3) {
											}
										}
										if (!__succ80) {
											Integer __predicateValue82 = hours_per_week == null? 3 : ((hours_per_week > 40)? 1 : 2);
											if (__predicateValue82 == 1) {
												__succ80 = true;
												income = ">50K";
											}
											else if (__predicateValue82 == 3) {
											}
										}
										if (!__succ80) {
											income = null;
											resultExplanation = null;
										}
									}
									else if (__predicateValue79 == 3) {
									}
								}
								if (!__succ78) {
									Integer __predicateValue83 = age == null? 3 : ((age > 26)? 1 : 2);
									if (__predicateValue83 == 1) {
										__succ78 = true;
										income = ">50K";
										Boolean __succ84 = false;
										if (!__succ84) {
											Integer __predicateValue85 = capital_gain == null? 3 : ((capital_gain <= 7688)? 1 : 2);
											if (__predicateValue85 == 1) {
												__succ84 = true;
												income = ">50K";
											}
											else if (__predicateValue85 == 3) {
											}
										}
										if (!__succ84) {
											Integer __predicateValue86 = capital_gain == null? 3 : ((capital_gain > 7688)? 1 : 2);
											if (__predicateValue86 == 1) {
												__succ84 = true;
												income = ">50K";
											}
											else if (__predicateValue86 == 3) {
											}
										}
										if (!__succ84) {
											income = null;
											resultExplanation = null;
										}
									}
									else if (__predicateValue83 == 3) {
									}
								}
								if (!__succ78) {
									income = null;
									resultExplanation = null;
								}
							}
							else if (__predicateValue77 == 3) {
							}
						}
						if (!__succ66) {
							income = null;
							resultExplanation = null;
						}
					}
					else if (__predicateValue65 == 3) {
					}
				}
				if (!__succ42) {
					income = null;
					resultExplanation = null;
				}
			}
			else if (__predicateValue41 == 3) {
			}
		}
		if (!__succ0) {
			income = null;
			resultExplanation = null;
		}

		return income;
	}
