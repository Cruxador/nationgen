package nationGen.misc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;









import java.util.Random;

import com.elmokki.Generic;
















import nationGen.entities.Entity;
import nationGen.entities.Filter;
import nationGen.entities.Pose;
import nationGen.entities.Race;
import nationGen.entities.Theme;
import nationGen.items.CustomItem;
import nationGen.items.Item;
import nationGen.nation.Nation;
import nationGen.units.ShapeChangeUnit;
import nationGen.units.Unit;

public class ChanceIncHandler {
	
	private Nation n;
	private Random r;
	public String identifier = "";
	
	public ChanceIncHandler(Nation n, String identifier)
	{
		this.identifier = identifier;
		this.n = n;
		this.r = new Random(n.random.nextInt());
		


	}
	
	public ChanceIncHandler(Nation n)
	{
		this.n = n;
		this.r = new Random(n.random.nextInt());
		

	}
	
	private List<String> getRaceThemeIncs(Race r)
	{
		if(r == null)
			return null;
	
		List<String> list = new ArrayList<String>();
		
		
		
		for(Theme t : r.themefilters)
			list.addAll(t.themeincs);
		
		return list;
	}
	
	public <T extends Filter> List<T> removeRelated(T thing, List<T> list)
	{
		
		List<T> shit = new ArrayList<T>();
		
		list.remove(thing);
		for(String type : thing.types)
			for(T t : list)
				if(t.types.contains(type))
				{
					shit.add(t);
					continue;
				}
		
		list.removeAll(shit);
		
		return list;
	}
	
	
	
	
	public <T extends Filter> LinkedHashMap<T, Double> handleChanceIncs(List<T> filters)
	{
		List<Unit> units = new ArrayList<Unit>();
		return handleChanceIncs(units, filters);
	}
	
	
	public <T extends Filter> LinkedHashMap<T, Double> handleChanceIncs(Unit u, List<T> filters)
	{
		return handleChanceIncs(u, filters, null);
	}
	
	public <T extends Filter> LinkedHashMap<T, Double> handleChanceIncs(Race r, String role, List<T> filters)
	{
		Unit u = new Unit(n.nationGen, r, this.getRandom(r.getPoses(role)));
		return handleChanceIncs(u, filters, null);
	}
	
	public <T extends Filter> LinkedHashMap<T, Double> handleChanceIncs(Unit u, List<T> filters, List<String> extraincs)
	{
		List<Unit> units = new ArrayList<Unit>();
		units.add(u);
		return handleChanceIncs(units, filters, extraincs);
	}
	
	public <T extends Filter> LinkedHashMap<T, Double> handleChanceIncs(List<Unit> u, List<T> filters)
	{
		return handleChanceIncs(u, filters, null);
	}
	
	/**
	 * The main method for handling chanceincs. This uses basechances.
	 * @param u
	 * @param filters
	 * @return
	 */
	public <T extends Filter> LinkedHashMap<T, Double> handleChanceIncs(List<Unit> u, List<T> filters, List<String> extraincs)
	{
		
		LinkedHashMap<T, Double> set = new LinkedHashMap<T, Double>();
		for(T t : filters)
		{
			set.put(t, t.basechance);
		}

		return handleChanceIncs(u, set, extraincs);
	}
	
	
	
	/**
	 * The main method for handling chanceincs. This overrides basechances.
	 * @param u
	 * @param filters
	 * @return
	 */
	public <T extends Filter> LinkedHashMap<T, Double> handleChanceIncs(Unit u, LinkedHashMap<T, Double> filters)
	{
		ArrayList<Unit> list = new ArrayList<Unit>();
		list.add(u);
		return handleChanceIncs(list, filters, null);
	}
	
	/**
	 * The main method for handling chanceincs. This overrides basechances.
	 * @param u
	 * @param filters
	 * @param extraincs
	 * @return
	 */
	public <T extends Filter> LinkedHashMap<T, Double> handleChanceIncs(List<Unit> u, LinkedHashMap<T, Double> set, List<String> extraincs)
	{
		
	
		Race race = null;
		if(u.size() > 0 && getRaceThemeIncs(u.get(0).race) != null)
			race = u.get(0).race;
		else if(n.races.size() > 0 && getRaceThemeIncs(n.races.get(0)) != null)
			race = n.races.get(0);
		
		


		List<String> miscincs = new ArrayList<String>();
		
		if(extraincs != null)
			miscincs.addAll(extraincs);
		
		for(Unit un : u)
		{
			for(Filter f : un.appliedFilters)
				miscincs.addAll(f.themeincs);
		}
		
		handleNationIncs(set, n, miscincs, race);

		
		// Actually handle the stuff
		for(Unit un : u)
		{
			
			List<String> unit_miscincs = new ArrayList<String>();
			for(Filter f : un.appliedFilters)
				unit_miscincs.addAll(f.themeincs);
			
			handleUnitIncs(set, un, unit_miscincs, race);
		}
		
		
		handleThemeIncs(set, miscincs, race);


		List<T> redundantFilters = new ArrayList<T>();
		
		for(T f : set.keySet())
		{
			if(set.get(f) <= 0)
				redundantFilters.add(f);
		}
		
		for(Filter f : redundantFilters)
		{
			if(set.keySet().contains(f))
				set.remove(f);
		}

		
		

		return set;
	}
	
	public static boolean suitableFor(Unit u, Filter f, Nation n)
	{
		for(String role : u.pose.roles)
		{
			List<String> noposes = Generic.getTagValues(f.tags, "nopose");
			for(String nopose : noposes)
				if(role.contains(nopose))
					return false;

		}
		
		if(n != null && n.races.size() > 0)
		{
			Race r = n.races.get(0);
			if(f.tags.contains("norace primary") && u.race == r)
				return false;
			if(f.tags.contains("norace secondary") && u.race != r)
				return false;
		}
		return true;
	}
	
	public static boolean canAdd(Unit u, Filter f)
	{
		if(!suitableFor(u, f, null))
		{
			return false;
		}
		
		List<String> primaries = new ArrayList<String>();		
		if(Generic.containsTag(f.tags, "primarycommand"))
		{
			for(String tag : f.tags)
			{
				List<String> args = Generic.parseArgs(tag);
				if(args.get(0).equals("primarycommand"))
				{
					primaries.add(args.get(1));
				}
			}
		}
		
		boolean shapeshift = false;
		for(Command c : f.getCommands())
			if(c.command.equals("#shapechange") || c.command.equals("#secondshape") || c.command.equals("#secondtmpshape"))
			{
				shapeshift = true;
				break;
			}
			
		boolean ok = false;
		boolean primarycommandfail = false;
		for(Command c : u.getCommands())
		{

			boolean tempok = true;
			if(primaries.contains(c.command))
			{
				primarycommandfail = true;
				ok = false;
				break;
			}
	
			if(shapeshift && (c.command.equals("#shapechange") || c.command.equals("#secondshape") || c.command.equals("#secondtmpshape")))
			{
				ok = false;
				break;
			}
			else if(shapeshift)

			
			for(Command fc : f.getCommands())
			{

				if(c.command.equals(fc.command) && c.args.size() == 0 && fc.args.size() == 0)
				{
					tempok = false;
					break;
				}
			}
			
			if(tempok)
				ok = true;
		}
		

		
		if(f.getCommands().size() == 0 && Generic.containsTag(f.tags, "lowenctreshold") && !primarycommandfail && ok)
		{
			int treshold = Integer.parseInt(Generic.getTagValue(f.tags, "lowenctreshold"));
			
			int enc = 0;
			if(u.getSlot("armor") != null)
				enc += u.nationGen.armordb.GetInteger(u.getSlot("armor").id, "enc");
			if(u.getSlot("offhand") != null && u.getSlot("offhand").armor)
				enc += u.nationGen.armordb.GetInteger(u.getSlot("offhand").id, "enc");
			if(u.getSlot("helmet") != null)
				enc += u.nationGen.armordb.GetInteger(u.getSlot("helmet").id, "enc");
			

			ok = (enc <= treshold);
			
			

		}
		
		if(!ok)
			return false;
		
		return canAdd(u.appliedFilters, f);
	}
	
	public static <E extends Filter> boolean canAdd(List<E> filters, Filter f)
	{



		// Forbid the same type
		for(Filter f2 : filters)
		{
			for(String s : f.types)
			{
				if(f2.types.contains(s))
				{
					
					return false;
				}
			}
		}  
		

		
		return true;
		

	}
	
	public static List<Filter> getFiltersWithPower(int min, int max, List<Filter> orig)
	{
		List<Filter> newList = new ArrayList<Filter>();
		for(Filter f : orig)
		{
			if(f.power <= max && f.power >= min)
				newList.add(f);
		}
		return newList;
	}
	
	
	public static <E extends Filter> List<E> getFiltersWithType(String type, List<E> orig)
	{
		List<E> newList = new ArrayList<E>();
		for(E f : orig)
		{
			if(f.types.contains(type))
			{
				newList.add(f);
			}
		}
		
		return newList;
	}
	
	
	/**
	 * Checks ChanceIncHandler.canAdd() for all filters and removes bad ones.
	 * @param filters
	 * @param units
	 * @return
	 */
	public static List<Filter> getValidUnitFilters(List<Filter> filters, List<Unit> units)
	{
		List<Filter> list = new ArrayList<Filter>();
		

		for(Filter f : filters)
		{

			boolean ok = true;
			for(Unit u : units)
			{

				if(!ChanceIncHandler.canAdd(u, f))
					ok = false;
			}
			
			if(ok)
				list.add(f);
		}
		
		return list;
	}
	
	public static List<Filter> getValidUnitFilters(List<Filter> filters, Unit unit)
	{
		List<Unit> l = new ArrayList<Unit>();
		l.add(unit);


		return getValidUnitFilters(filters, l);
	}
	/**
	 * Checks ChanceIncHandler.canAdd() for all filters and removes bad ones.
	 * @param filters
	 * @param units
	 * @return
	 */
	public static <E extends Filter> List<E> getValidFilters(List<E> filters, List<E> oldfilters)
	{
		List<E> list = new ArrayList<E>();
		
		for(E f : filters)
		{
			if(ChanceIncHandler.canAdd(oldfilters, f))
				list.add(f);
	
		}
		
		return list;
	}
	
	
	private static <E extends Filter> void removeRemovableFilters(List<E> filters)
	{
		List<E> remov = new ArrayList<E>();
		for(E e : filters)
			if(!e.tags.contains("unremovable"))
				remov.add(e);
		
		filters.removeAll(remov);
	}
	
	
	private static <E extends Filter> List<E> retrieveFiltersFromTags(List<String> tags, String lookfor, ResourceStorage<E> source)
	{
		List<E> filters = new ArrayList<E>();
		
		for(String tag : tags)
		{
			if(tag.startsWith(lookfor))
			{
				
				String setname = tag.split(" ")[1];
				List<E> set = source.get(setname);
				
				if(setname.equals("clear"))
				{
					removeRemovableFilters(filters);
					if(set != null)
						System.out.println("WARNING! #" + lookfor + " clear is trying to load a set named clear. This did not succeed. Please rename the set.");
				}
				else if(set != null)
				{
					filters.addAll(set);
				}
				else
					System.out.println("#" + lookfor + " " + setname + " could not find the set " + setname);
			
			
			}
		}	
		
		return filters;
	}
	
	
	public static <E extends Filter> List<E> retrieveFilters(String lookfor, String defaultset, ResourceStorage<E> source, Pose p, Race r)
	{
		String[] derp = {defaultset};
		return retrieveFilters(lookfor, derp, source, p, r);
	}
	
	public static <E extends Filter> List<E> retrieveFilters(String lookfor, String[] defaultset, ResourceStorage<E> source, Pose p, Race r)
	{
		List<E> filters = new ArrayList<E>();
		
		
		if(r != null)
			filters.addAll(retrieveFiltersFromTags(r.tags, lookfor, source));
		
		if(p != null)
			filters.addAll(retrieveFiltersFromTags(p.tags, lookfor, source));

	

		
		
		if(filters.size() == 0)
		{
			for(String str : defaultset)
			{
				if(source.get(str) != null)
					filters.addAll(source.get(str));
				else
					System.out.println("Default set " + str + " for " + lookfor + " was not found from " + source);
			}

		}

		
		return filters;

	}
	
	private List<Integer> pathsAtHighest(int[] unitpaths)
	{
		List<Integer> atHighest = new ArrayList<Integer>();
		int highest = 0;
		for(int i = 5; i > 0; i--)
		{
			highest = i;
			int[] paths = unitpaths;
			for(int j = 0; j < 9; j++)
			{
				if(paths[j] == i && !atHighest.contains(j))
				{
					atHighest.add(j);
				}
			}
			if(atHighest.size() > 0)
				break;
			
			atHighest.clear();
		}
		
		List<Integer> atSecondHighest = new ArrayList<Integer>();
		int secondHighest = 0;
		for(int i = highest - 1; i > 0; i--)
		{
			secondHighest = i;
			int[] paths = unitpaths;
			for(int j = 0; j < 9; j++)
			{
				if(paths[j] == i && !atHighest.contains(j))
				{
					atSecondHighest.add(j);
				}
			}
			if(atSecondHighest.size() > 0)
				break;
			
			atSecondHighest.clear();
		}
		
		if(highest - secondHighest == 1 && atHighest.size() == 1 && atHighest.size() + atSecondHighest.size() < 4)
		{
			atHighest.addAll(atSecondHighest);
		}
		
		return atHighest;
	}
	
	
	
	private double applyMod(double value, String modifier)
	{
		if(modifier.startsWith("+"))
		{
			modifier = modifier.substring(1);
			value += Double.parseDouble(modifier);
		}
		else if(modifier.startsWith("*"))
		{
			modifier = modifier.substring(1);
			value *= Double.parseDouble(modifier);
		}
		else if(modifier.startsWith("/"))
		{
			modifier = modifier.substring(1);
			value /= Double.parseDouble(modifier);
		}
		else
		{

			value += Double.parseDouble(modifier);
		}
		return value;
	}
	
	
	
	
	public Double applyModifier(double value, String modifier)
	{
		String[] mod = modifier.split(" or ");
		double[] results = new double[mod.length];
		
		if(mod.length == 0)
			return value;
		
		boolean max = true;
		if(modifier.startsWith("max("))
			modifier = modifier.substring(4, modifier.length() - 2);
		if(modifier.startsWith("min("))
		{
			modifier = modifier.substring(4, modifier.length() - 2);
			max = false;
		}
		
		

		double biggest = applyMod(value, mod[0]);;
		for(int i = 0; i < results.length; i++)
		{
			results[i] = applyMod(value, mod[i]);
			
			
			if(results[i] > biggest && max)
				biggest = results[i];
			else if(results[i] < biggest && !max)
				biggest = results[i];
		}
		
		return biggest;
			
	}
	
	/**
	 * This is a separate method for chanceincs that target the filter/item/whatever itself - ie stuff that is relevant 
	 * for themes only since in general targetting the thing itself is pointless.
	 * 
	 * All theme chanceincs SHOULD NOT be implemented here. If the chanceinc does not target the filter itself,
	 * for example not something like "higher chance if item contains theme 'advanced'", it is usable in other
	 * places as well.
	 * 
	 * @param filters
	 * @param n
	 */
	private <T extends Filter> void handleThemeIncs(LinkedHashMap<T, Double> filters, List<String> miscincs, Race r)
	{
		
		List<String> chanceincs = new ArrayList<String>();
		
		// Add race themes if appliceable!
		if(r != null)
			chanceincs.addAll(getRaceThemeIncs(r)); // Should never be null.

		// Add all nation theme themeincs!
		for(Theme t : n.nationthemes)
			chanceincs.addAll(t.themeincs);
		
		chanceincs.addAll(miscincs);
		
		for(T f : filters.keySet())
		{
	
			for(String str : chanceincs)
			{
				List<String> args = Generic.parseArgs(str, "'");
	
				// Theme
				if(args.get(0).equals("theme") && args.size() >= 3)
				{
					boolean not = args.contains("not");
					if(f.themes.contains(args.get(args.size() - 2)) != not)
					{
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
					}
				}
				else if(args.get(0).equals("owntag") && args.size() >= 3)
				{
					boolean not = args.contains("not");
					if(f.tags.contains(args.get(args.size() - 2)) != not)
					{
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
					}
				}
				else if(args.get(0).equals("racename") && args.size() >= 3 && f.name != null)
				{
					boolean not = args.contains("not");
	
					if(f.name.toLowerCase().equals(args.get(args.size() - 2).toLowerCase()) != not)
					{
						if(f.getClass().equals(Race.class))
						{
							applyChanceInc(filters, f,  (args.get(args.size() - 1)));

						}
					}
				}
				else if(args.get(0).equals("thisitemtag") && args.size() >= 3 && f.name != null)
				{
					boolean not = args.contains("not");
					if(f.tags.contains(args.get(args.size() - 2)) != not)
					{
						if(f.getClass().equals(Item.class) || f.getClass().equals(CustomItem.class))
						{
							applyChanceInc(filters, f,  (args.get(args.size() - 1)));
						}
					}
				}
				else if(args.get(0).equals("thisitemslottag") && args.size() >= 3 && f.name != null)
				{
					boolean not = args.contains("not");
					if(f.tags.contains(args.get(args.size() - 2)) != not)
					{
						if(f.getClass().equals(Item.class) || f.getClass().equals(CustomItem.class))
						{
							Item i = (Item)f;
							if(i.slot.equals(args.get(args.size() - 3)))
							{
								applyChanceInc(filters, f,  (args.get(args.size() - 1)));
							}
						}
					}
				}
				else if(args.get(0).equals("thisitemtheme") && args.size() >= 3 && f.name != null)
				{
					boolean not = args.contains("not");
	
					if(f.themes.contains(args.get(args.size() - 2)) != not)
					{
						if(f.getClass().equals(Item.class) || f.getClass().equals(CustomItem.class))
						{
							applyChanceInc(filters, f,  (args.get(args.size() - 1)));
						}
					}
				}
				else if(args.get(0).equals("thisitemslottheme") && args.size() >= 3 && f.name != null)
				{
					boolean not = args.contains("not");
					if(f.themes.contains(args.get(args.size() - 2)) != not)
					{
						if(f.getClass().equals(Item.class) || f.getClass().equals(CustomItem.class))
						{
							Item i = (Item)f;
							if(i.slot.equals(args.get(args.size() - 3)))
							{
								applyChanceInc(filters, f,  (args.get(args.size() - 1)));
							}
						}
					}
				}
				else if(args.get(0).equals("isferrousitem") && args.size() >= 2 && f.name != null)
				{
					boolean not = args.contains("not");
					if(f.getClass().equals(Item.class) || f.getClass().equals(CustomItem.class))
					{
						Item i = (Item)f;
						if(i.armor)
						{
							int ferrous = n.nationGen.armordb.GetInteger(i.id, "ferrous", 0);
							if((ferrous == 1) != not)
							{
								applyChanceInc(filters, f,  (args.get(args.size() - 1)));
							}
						}
						else
						{
							int ferrous = n.nationGen.weapondb.GetInteger(i.id, "ironweapon", 0);
							if((ferrous == 1) != not)
							{
								applyChanceInc(filters, f,  (args.get(args.size() - 1)));
							}
						}
					}
				}
				else if(args.get(0).equals("weaponuwpenalty") && args.size() >= 3 && f.name != null)
				{
					boolean below = args.contains("below");
					if(f.getClass().equals(Item.class) || f.getClass().equals(CustomItem.class))
					{
						Item i = (Item)f;

						int value = Integer.parseInt(args.get(args.size() - 2));
						int penalty = 0;
						
						if(!i.armor)
						{
							int slash = n.nationGen.weapondb.GetInteger(i.id, "dt_slash", 0);
							int blunt = n.nationGen.weapondb.GetInteger(i.id, "dt_blunt", 0);
							int pierce = n.nationGen.weapondb.GetInteger(i.id, "dt_pierce", 0);
							int lgt = n.nationGen.weapondb.GetInteger(i.id, "lgt", 0);

							if(pierce == 0 && (blunt == 1 || slash == 1))
								penalty = lgt;
							else if(pierce == 1 && (blunt == 1 || slash == 1))
								penalty = (int) Math.round((double)lgt / 2);
							
							if((penalty >= value) != below)
							{
								applyChanceInc(filters, f,  (args.get(args.size() - 1)));
							}
						}
					}
				}
				else if(args.get(0).equals("thisarmorprot") && args.size() >= 3 && f.name != null)
				{
					boolean below = args.contains("below");

					if(f.getClass().equals(Item.class) || f.getClass().equals(CustomItem.class))
					{
						int value = Integer.parseInt(args.get(args.size() - 2));
						Item i = (Item)f;
						
						if(i.armor && !i.slot.equals("offhand"))
						{
							int prot = n.nationGen.armordb.GetInteger(i.id, "prot", 0);
							if((prot >= value) != below)
							{
								applyChanceInc(filters, f,  (args.get(args.size() - 1)));
							}
						}
					}
				}
				else if(args.get(0).equals("thisarmorenc") && args.size() >= 3 && f.name != null)
				{
					boolean below = args.contains("below");

					if(f.getClass().equals(Item.class) || f.getClass().equals(CustomItem.class))
					{
						int value = Integer.parseInt(args.get(args.size() - 2));
						Item i = (Item)f;
						
						if(i.armor && !i.slot.equals("offhand"))
						{
							int prot = n.nationGen.armordb.GetInteger(i.id, "enc", 0);
							if((prot >= value) != below)
							{
								applyChanceInc(filters, f,  (args.get(args.size() - 1)));
							}
						}
					}
				}
				else if(args.get(0).equals("thisarmordb") && args.size() >= 3 && f.name != null)
				{
					boolean below = args.contains("below");
					String query = args.get(args.size() - 3);
					
					if(f.getClass().equals(Item.class) || f.getClass().equals(CustomItem.class))
					{
						int value = Integer.parseInt(args.get(args.size() - 2));
						Item i = (Item)f;
						
						if(i.armor)
						{
							int gotvalue = n.nationGen.armordb.GetInteger(i.id, query);
							
							if((gotvalue >= value) != below)
							{
								applyChanceInc(filters, f,  (args.get(args.size() - 1)));
							}
						}
					}
				}
				else if(args.get(0).equals("thisweapondb") && args.size() >= 3 && f.name != null)
				{
					boolean below = args.contains("below");
					String query = args.get(args.size() - 3);
					
					if(f.getClass().equals(Item.class) || f.getClass().equals(CustomItem.class))
					{
						int value = Integer.parseInt(args.get(args.size() - 2));
						Item i = (Item)f;
						
						if(!i.armor)
						{
							int gotvalue = n.nationGen.weapondb.GetInteger(i.id, query);
							
							if((gotvalue >= value) != below)
							{
								applyChanceInc(filters, f,  (args.get(args.size() - 1)));
							}
						}
					}
				}
		
			}
			
			
		}
	}

	public <T extends Filter> List<T> getPossibleFilters(List<T> list, Unit u)
	{
		List<T> stuff = new ArrayList<T>();
		stuff.addAll(handleChanceIncs(u, list).keySet());
		return stuff;
	}
	
	public <T extends Filter> List<T> getPossibleFilters(List<T> list)
	{
		List<T> stuff = new ArrayList<T>();
		stuff.addAll(handleChanceIncs(list).keySet());
		return stuff;
	}
	
	public <T extends Filter> int countPossibleFilters(List<T> list, Unit u)
	{
	
		return 	this.handleChanceIncs(u, list).size();
	}
	
	public <T extends Filter> int countPossibleFilters(List<T> list)
	{
	
		return 	this.handleChanceIncs(list).size();
	}
	
	public <T extends Filter> T getRandom(List<T> list, List<Unit> units)
	{
		return Entity.getRandom(r, this.handleChanceIncs(units, list));
	}
	
	public <T extends Filter> T getRandom(List<T> list, Unit u)
	{
		return Entity.getRandom(r, this.handleChanceIncs(u, list));
	}
	
	public <T extends Filter> T getRandom(List<T> list, Race race, String role)
	{
		return Entity.getRandom(r, this.handleChanceIncs(race, role, list));
	}
	
	public <T extends Filter> T getRandom(List<T> list)
	{
		return Entity.getRandom(r, this.handleChanceIncs(list));
	}


	private <T extends Filter> void applyChanceInc(LinkedHashMap<T, Double> filters, T f,  String mod)
	{
		if(!filters.containsKey(f))
		{
			filters.put(f, f.basechance);
		}
		
		double a = applyModifier(filters.get(f), mod);
		filters.put(f, a);

	}
	
	private <T extends Filter> void applyChanceInc(LinkedHashMap<T, Double> filters, T f,  double value)
	{
		if(!filters.containsKey(f))
		{
			filters.put(f, f.basechance);
		}
		
		filters.put(f, value);

	}
	
	private <T extends Filter> void handleNationIncs(LinkedHashMap<T, Double> filters,  Nation n, List<String> miscincs, Race race)
	{

		List<Unit> tempmages = n.generateComList("mage");
		
		int[] paths = new int[9];
		for(Unit u : tempmages)
		{						
			for(String tag : u.tags)
				if(tag.startsWith("schoolmage"))
				{
					int[] picks = u.getMagicPicks(true);
					for(int j = 0; j < 9; j++)
					{
						if(paths[j] < picks[j])
							paths[j] = picks[j];
					}
				}
		}
		
		int[] nonrandom_paths = new int[9];
		for(Unit u : tempmages)
		{						
			for(String tag : u.tags)
				if(tag.startsWith("schoolmage"))
				{
					int[] picks = u.getMagicPicks(false);
					for(int j = 0; j < 9; j++)
					{
						if(nonrandom_paths[j] < picks[j])
							nonrandom_paths[j] = picks[j];
					}
				}
		}
		
		List<Integer> atHighest = this.pathsAtHighest(paths);

		int diversity = 0;
		int[] at = new int[10];
		for(int i = 0; i < 9; i++)
		{
			if(paths[i] > 1 || (i == 4 && paths[i] == 1) || (i == 7 && paths[i] == 1))
				diversity++;
		
			if(i < 10)
				at[i]++;
		}
		
		
		// Avg resources and gold
		int unitCount = 0;
		double totalgold = 0;
		double totalres = 0;
		
		for(Unit u : n.generateTroopList())
		{
			unitCount++;
			if(u.pose.roles.contains("mounted") || u.pose.roles.contains("chariot"))
				totalgold += u.getGoldCost()  * 0.66;
			else
				totalgold += u.getGoldCost();
			
			totalres += u.getResCost(true);
		}
		
		double avgres = totalres / unitCount;
		double avggold = totalgold / unitCount;
		
		
		
		
		// Do chanceincs!
		for(T f : filters.keySet())
		{
			
			List<String> chanceincs = new ArrayList<String>();
			chanceincs.addAll(f.chanceincs);
			chanceincs.addAll(miscincs);
			

			// Should never be null.
			if(race != null)
				chanceincs.addAll(getRaceThemeIncs(race));
			
			for(String str : chanceincs)
			{
				
				List<String> args = Generic.parseArgs(str, "'");
	
				// Magic paths
				boolean canIncrease = true;
				if(args.get(0).equals("magic") && args.size() >= 3)
				{
					boolean not = false;
					for(int i = 1; i < args.size() - 1; i++)
					{
						if(args.get(i).equals("not"))
						{
							not = true;
							continue;
						}
						
						int path = Generic.PathToInteger(args.get(i));
						
						
						if(atHighest.contains(path) == not)
						{
							canIncrease = false;
							break;
						}
						
						not = false;
						
					}
					
					if(canIncrease)
					{
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
					}
				}
				
				// Mage with paths
				canIncrease = false;
				if(args.get(0).equals("magewithpaths") && args.size() >= 3)
				{
					for(Unit u : n.generateComList("mage"))
					{
						boolean fine = true;

						boolean not = false;
						for(int i = 1; i < args.size() - 1; i++)
						{
							if(args.get(i).equals("not"))
							{
								not = true;
								continue;
							}
							
							int path = Generic.PathToInteger(args.get(i));
							int[] allpaths = u.getMagicPicks();
							
							
									
							if(allpaths[path] > 0 == not)
							{
								fine = false;
								break;
							}
							
							not = false;
							
						}
						
						if(fine)
						{
							canIncrease = true;
							break;
						}
					}
					
					if(canIncrease)
					{
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
					}
				}
				
				// Any magic
				canIncrease = true;
				if(args.get(0).equals("anymagic") && args.size() >= 3)
				{
					
					boolean not = args.contains("not");
					for(int i = 1; i < args.size(); i++)
					{
						int path = Generic.PathToInteger(args.get(args.size() - 2));
						if(path < 0)
							continue;
						
						if(nonrandom_paths[path] == 0)
							canIncrease = false;	
						
					}
				
					if(canIncrease != not)
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
				}
				
				// Existing shapeshifts
				canIncrease = false;
				if(args.get(0).equals("shape") && args.size() >= 3)
				{
					String shape = args.get(1);
					for(ShapeChangeUnit u : n.secondshapes)
					{
						if(u.thisForm.name.equals(shape))
							canIncrease = true;			
					}
					
					if(canIncrease)
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
				}
				
				// Magic diversity
				canIncrease = false;
				if(args.get(0).equals("magicdiversity"))
				{
					int div = Integer.parseInt(args.get(args.size() - 2));
					boolean not = args.contains("not");
					
					if((diversity >= div) != not)
						canIncrease = true;
					
					if(canIncrease)
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
				}
				
				// Magic diversity
				canIncrease = false;
				if(args.get(0).equals("picksatlevel"))
				{
					int level = Integer.parseInt(args.get(args.size() - 3));
					int amount = Integer.parseInt(args.get(args.size() - 2));

					boolean not = args.contains("not");
					
					int picks = 0;
					for(int z = level; z < 10; z++)
						picks += at[z];
					
					if(level < 5 && (picks >= amount) != not)
						canIncrease = true;
					
					if(canIncrease)
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
				}
				
				// Spell(sets)
				canIncrease = false;
				if(args.get(0).equals("spells") && args.size() >= 3)
				{
					String name = args.get(1);
					for(Filter s : n.spells)
					{
						if(s.name.toLowerCase().equals(name.toLowerCase()))
							canIncrease = true;
					}
					
					if(canIncrease)
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
				}
				// ModuleID
				if(args.get(0).equals("moduleid") && args.size() >= 3)
				{
					String id = args.get(1);
					if(id.equals(this.identifier))
					{
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
					}
					
					
				}
				
				// Primary race theme
				canIncrease = false;
				if(args.get(0).equals("hastheme") && args.size() >= 3 && n.races.size() > 0)
				{
					String theme = args.get(args.size() - 2);
					boolean not = args.contains("not");

					for(Theme t : n.races.get(0).themefilters)
					{
						if(t.name.equals(theme))
							canIncrease = true;			
					}
					
					if(canIncrease != not)
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
				}
				
				// Theme in a primary race theme
				canIncrease = false;
				if(args.get(0).equals("anytheme") && args.size() >= 3 && n.races.size() > 0)
				{
					String theme = args.get(args.size() - 2);
					boolean not = args.contains("not");
					
					for(Theme t : n.races.get(0).themefilters)
					{
						if(t.themes.contains(theme))
							canIncrease = true;			
					}
					if(n.races.get(0).themes.contains(theme))
						canIncrease = true;		
					
					if(canIncrease != not)
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
				}
				
				// Theme in a primary race theme
				canIncrease = false;
				if(args.get(0).equals("hasthemetheme") && args.size() >= 3 && n.races.size() > 0)
				{
					String theme = args.get(args.size() - 2);
					boolean not = args.contains("not");
					
					for(Theme t : n.races.get(0).themefilters)
					{
						if(t.themes.contains(theme))
							canIncrease = true;			
					}
					
					if(canIncrease != not)
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
				}
				
				// Theme in a primary race theme
				canIncrease = false;
				if(args.get(0).equals("secondarythemetheme") && args.size() >= 3 && n.races.size() > 1)
				{
					String theme = args.get(args.size() - 2);
					boolean not = args.contains("not");
					
					for(Theme t : n.races.get(1).themefilters)
					{
						if(t.themes.contains(theme))
							canIncrease = true;			
					}
					
					if(canIncrease != not)
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
				}
				
				// SecondaryRaceTheme
				canIncrease = false;
				if(args.get(0).equals("secondaryracetheme") && args.size() >= 3 && n.races.size() > 1)
				{
					String theme = args.get(args.size() - 2);
					boolean not = args.contains("not");
					
					for(Theme t : n.races.get(1).themefilters)
					{
						if(t.name.equals(theme))
							canIncrease = true;			
					}
					
					if(canIncrease != not)
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
				}
				
				// Era
				canIncrease = false;
				if(args.get(0).equals("era") && args.size() >= 3)
				{
					int era = Integer.parseInt(args.get(1));
					if(era == n.era)
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
				}
				// Primary race
				canIncrease = false;
				if(args.get(0).equals("hasprimaryrace") && args.size() >= 2)
				{
					if(n.races.size() > 0)
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
				}
				
				// Nation commands
				canIncrease = false;
				if(args.get(0).equals("nationcommand") && args.size() >= 3)
				{					

					String command = args.get(1);

					int dir = 0;
					if(args.contains("below"))
						dir = -1;
					if(args.contains("above"))
						dir = 1;
					
					
						
					List<Command> coms = n.getCommands();
					coms.addAll(n.races.get(0).nationcommands);
					for(Command c : coms)
					{
						if(c.command.equals(command) || c.command.equals("#" + command))
						{
							
							String arg = c.args.get(0);

							
							if(dir == -1 && Integer.parseInt(arg) < Integer.parseInt(args.get(args.size() - 2)))
							{
								canIncrease = true;
							}
							else if(dir == 1 && Integer.parseInt(arg) > Integer.parseInt(args.get(args.size() - 2)))
							{
								canIncrease = true;
							}
							else if(dir == 0)
							{
								canIncrease = true;
							}
								
						}
					}
					
			

					
					if(canIncrease)
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
				}
				
				
				// Magic priority
				canIncrease = false;
				if(args.get(0).equals("magicpriority"))
				{
					List<String> tags = Generic.getTags(Generic.getAllNationTags(n), "magicpriority");
					double chance = 1;
					for(String str1 : tags)
					{
						List<String> args1 = Generic.parseArgs(str1);
						if(args.get(1).equals(args1.get(1)))
						{
							chance *= Double.parseDouble(args1.get(2));
						}
					}
					
					if(args.contains("below") && chance < Double.parseDouble(args.get(args.size() - 2)))
						canIncrease = true;
					else if(chance >= Double.parseDouble(args.get(args.size() - 2)))
						canIncrease = true;
					
					
					if(canIncrease)
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
				}
				
				
				// Nation commands
				canIncrease = false;
				if(args.get(0).equals("primaryracecommand") && args.size() >= 3)
				{					

					String command = args.get(1);

					int dir = 0;
					if(args.contains("below"))
						dir = -1;
					if(args.contains("above"))
						dir = 1;
					
					
						
					List<Command> coms = n.races.get(0).getCommands();
					for(Command c : coms)
					{
						if(c.command.equals(command) || c.command.equals("#" + command))
						{
							
							String arg = c.args.get(0);

							
							if(dir == -1 && Integer.parseInt(arg) < Integer.parseInt(args.get(args.size() - 2)))
							{
								canIncrease = true;
							}
							else if(dir == 1 && Integer.parseInt(arg) > Integer.parseInt(args.get(args.size() - 2)))
							{
								canIncrease = true;
							}
							else if(dir == 0)
							{
								canIncrease = true;
							}
								
						}
					}
					
			

					
					if(canIncrease)
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
				}
				
				// Unit commands
				canIncrease = false;
				if(args.get(0).equals("command") && args.size() >= 3)
				{
					String command = args.get(1);
					if(!command.startsWith("#"))
						command = "#" + command;
					
					List<Unit> units = n.generateTroopList();
					units.addAll(n.generateComList("mage"));
					units.addAll(n.generateComList("priest"));
					
					for(Unit u : units)
					{
						for(Command c : u.getCommands())
						{
							if(c.command.equals(command) || c.command.equals("#" + command))
							{
								if(args.size() > 3)
								{
									int level = 0;
									boolean above = true;
									if(Generic.isNumeric(args.get(2)))
									{
										level = Integer.parseInt(args.get(2));
									}
									else if(args.size() > 4)
									{
										if(args.contains("below"))
											above = false;
										
										level = Integer.parseInt(args.get(3));
									}
									else
										canIncrease = true;
									
									
									if(!canIncrease)
									{
										if((Integer.parseInt(c.args.get(0)) >= level) == above)
										{
											canIncrease = true;
										}

										
									}
									
								}
								else
									canIncrease = true;
							}
							
						}
					}
					
		
					if(canIncrease)
					{
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
					}
				}
				
				// Percentage of command
				canIncrease = false;
				if(args.get(0).equals("percentageofcommand") && args.size() >= 3)
				{
					double all = 0;
					double found = 0;
					
					String command = args.get(1);
					List<Unit> units = n.generateTroopList();
					units.addAll(n.generateComList("mage"));
					units.addAll(n.generateComList("priest"));
					
					for(Unit u : units)
					{
						all++;
						for(Command c : u.getCommands())
						{
							if(c.command.equals(command) || c.command.equals("#" + command))
							{
								found++;
							}
							
						}
					}
					
					double share = found/all;
					boolean below = args.contains("below");
					double required = Double.parseDouble(args.get(args.size() - 2));
					if((share < required) == below)
						canIncrease = true;
					
					if(canIncrease)
					{
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
					}
				}
				
				// Percentage of race
				canIncrease = false;
				if(args.get(0).equals("percentageofrace") && args.size() >= 3)
				{

					Race r = null;
					for(Race r2 : n.races)
						if(args.get(1).toLowerCase().equals(r2.name.toLowerCase()))
								r = r2;
					
					if(r != null)
					{
						double share = n.percentageOfRace(r);
						boolean below = args.contains("below");
						double required = Double.parseDouble(args.get(args.size() - 2));
						if((share < required) == below)
							canIncrease = true;
					}
					
					if(canIncrease)
					{
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
					}
				}
				
				// Race
				canIncrease = false;
				if(args.get(0).equals("primaryrace") && args.size() >= 3)
				{
					boolean not = args.contains("not");
					if(n.races.size() > 0 && n.races.get(0).name.toLowerCase().equals(args.get(args.size() - 2).toLowerCase()))
					{
						canIncrease = true;
					}
					if(canIncrease != not)
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
				}
				
				canIncrease = false;
				if(args.get(0).equals("secondaryrace") && args.size() >= 3)
				{
					boolean not = args.contains("not");
					if(n.races.size() > 1 && n.races.get(1).name.toLowerCase().equals(args.get(args.size() - 2).toLowerCase()))
					{
						canIncrease = true;
					}
					if(canIncrease != not)
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
				}
				
				// racetag
				canIncrease = false;
				if(args.get(0).equals("racetag") && args.size() >= 3 && n.races.size() > 0)
				{
					if(n.races.get(0).tags.contains(args.get(1)))
						canIncrease = true;
					
					if(canIncrease)
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
				}
				
				// any unit filter
				canIncrease = false;
				if(args.get(0).equals("anyunitfilter"))
				{
					for(Unit u : n.generateTroopList())
						for(Filter fa : u.appliedFilters)
							if(fa.name.equals(args.get(1)))
								canIncrease = true;
					for(Unit u : n.generateComList())
						for(Filter fa : u.appliedFilters)
							if(fa.name.equals(args.get(1)))
								canIncrease = true;
					
					if(canIncrease)
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
				}
				// nationtag
				canIncrease = false;
				if(args.get(0).equals("nationtag") && args.size() >= 3)
				{
					
					for(Filter f2 : n.nationthemes)
						if(f2.tags.contains(args.get(1)))
							canIncrease = true;
					
					if(n.races.size() > 0 && n.races.get(0).tags.contains(args.get(1)))
						canIncrease = true;
					
					if(canIncrease)
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
				}
				
				// Average gold and res
				canIncrease = false;
				if(args.get(0).equals("avggold") && args.size() >= 3)
				{
					double gold = Double.parseDouble(args.get(1));
					if(avggold >= gold)
						canIncrease = true;
					
					if(canIncrease)
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
				}
				
				canIncrease = false;
				if(args.get(0).equals("avgres") && args.size() >= 3)
				{
					double res = Double.parseDouble(args.get(1));
					if(avgres >= res)
						canIncrease = true;
					
					if(canIncrease)
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
				}
				
				canIncrease = false;
				if(args.get(0).equals("unitswithresabove") && args.size() >= 3)
				{
					boolean below = args.contains("below");
					double res = Double.parseDouble(args.get(args.size() - 3));
					int count = Integer.parseInt(args.get(args.size() - 2));
					
					int counted = 0;
					for(Unit u : n.generateTroopList())
					{
						if(u.getResCost(true) > res)
							counted++;
					}
					
					if((counted >= count) != below)
						canIncrease = true;
					
					if(canIncrease)
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
				}
				
				canIncrease = false;
				if(args.get(0).equals("caponlyunitswithresabove") && args.size() >= 3)
				{
					boolean below = args.contains("below");
					double res = Double.parseDouble(args.get(args.size() - 3));
					int count = Integer.parseInt(args.get(args.size() - 2));
					
					int counted = 0;
					for(Unit u : n.generateTroopList())
					{
						if(u.getResCost(true) > res && u.caponly)
							counted++;
					}
					
					if((counted >= count) != below)
						canIncrease = true;
					
					if(canIncrease)
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
				}
				
				
				canIncrease = false;
				if(args.get(0).equals("unitswithsize") && args.size() >= 3)
				{
					boolean below = args.contains("below");
					double size = Double.parseDouble(args.get(args.size() - 3));
					int count = Integer.parseInt(args.get(args.size() - 2));
					
					int counted = 0;
					for(Unit u : n.generateTroopList())
					{
						if(u.getCommandValue("#size", 2) >= size)
							counted++;
					}
					
					if((counted >= count) != below)
						canIncrease = true;
					
					if(canIncrease)
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
				}
				
				// Random
				canIncrease = false;
				if(args.get(0).equals("random") && args.size() >= 3)
				{
					double res = Double.parseDouble(args.get(1));
					if(r.nextDouble() < res)
						canIncrease = true;
					
					if(canIncrease)
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
				}
			}

		}
		

		// Do national affinities
		if(n.races.size() > 0)
			for(String tag : n.races.get(0).tags)
			{
				List<String> args = Generic.parseArgs(tag);
				if(args.get(0).equals("filteraffinity") && args.size() > 3)
				{
					double multi = Double.parseDouble(args.get(args.size() - 1));
					String value = args.get(args.size() - 2);
					boolean type = false;
					
					if(args.contains("type"))
						type = true;
					else if(args.contains("name")){/*do nothing*/}
					else
						System.out.println("Invalid #filteraffinity (no type/name): " + tag);
						
					for(T f : filters.keySet())
					{
						if(type && f.types.contains(value))
							this.applyChanceInc(filters, f, f.basechance * multi);
						else if(!type && f.name.equals(value))
							this.applyChanceInc(filters, f, f.basechance * multi);
					}
				}
	
			}
		

	}

	
	protected boolean alreadyHasType(List<? extends Filter> filters, List<String> types)
	{
		if(types.size() == 0)
			return false;
		
		for(Filter f : filters)
			for(String type : types)
				if(f.types.contains(type) && !f.tags.contains("freetype"))
					return true;
		
		return false;
	}
	
	protected boolean alreadyHasType(List<? extends Filter> filters, String type)
	{
		if(type.equals(""))
			return false;
		
		for(Filter f : filters)
			if(f.types.contains(type) && !f.tags.contains("freetype"))
				return true;
		
		return false;
	}
	
	
	private <T extends Filter> void handleUnitIncs(LinkedHashMap<T, Double> filters,  Unit u, List<String> miscincs, Race race)
	{

		if(u == null)
			return;

		for(T f : filters.keySet())
		{	
			


			
			List<String> chanceincs = new ArrayList<String>();
			chanceincs.addAll(f.chanceincs);
			chanceincs.addAll(miscincs);

			
			// Should never be null.
			if(race != null)
				chanceincs.addAll(getRaceThemeIncs(race));
			
			
			for(String str : chanceincs)
			{
		
				// Poses
				List<String> args = Generic.parseArgs(str, "'");
				if(args.get(0).equals("pose") && args.size() > 2)
				{
				
					boolean not = args.contains("not");
					boolean contains = false;
					if(u.pose.roles.contains(args.get(args.size() - 2)) || u.pose.roles.contains("elite " + args.get(args.size() - 2)) || u.pose.roles.contains("sacred " + args.get(args.size() - 2)))
					{
						contains = true;
					}
					
				
					
					if(contains != not)
					{
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
						continue;
					}
				}
				else if(args.get(0).equals("personalcommand") && args.size() > 2)
				{

					boolean canIncrease = false;
					String command = args.get(1);

					for(Command c : u.getCommands())
					{

						if(c.command.equals(command) || c.command.equals("#" + command))
						{
							if(args.size() > 3)
							{
								int level = 0;
								boolean above = true;
								boolean exact = false;
								if(Generic.isNumeric(args.get(2)))
								{
									level = Integer.parseInt(args.get(2));
								}
								else if(args.size() > 4)
								{
									if(args.contains("below"))
										above = false;
									else if(args.contains("exact"))
										exact = true;
									
									level = Integer.parseInt(args.get(3));
								}
								else
									canIncrease = true;
								
								
								if(!canIncrease)
								{
									if(!exact && (Integer.parseInt(c.args.get(0)) >= level) == above)
									{
										canIncrease = true;
									}
									else if(exact && (Integer.parseInt(c.args.get(0)) == level))
									{
										canIncrease = true;
									}

									
								}
								
							}
							else
								canIncrease = true;
						}
						
					}
					if(canIncrease)
					{

						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
						continue;
					}
				}
				if(args.get(0).equals("personalshape") && args.size() >= 3)
				{
					boolean contains = false;
					String shape = args.get(1);
					for(ShapeChangeUnit u2 : n.secondshapes)
					{
						if(u2.thisForm.name.equals(shape) && u2.otherForm.equals(u))
							contains = true;			
					}
					
					if(contains)
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
				}
				if(args.get(0).equals("filter") && args.size() >= 3)
				{
					boolean contains = false;
					for(Filter f2 : u.appliedFilters)
					{
						if(f2.name.equals(args.get(1)))
							contains = true;
					}
					if(contains)
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
				}
				else if(args.get(0).equals("unittag") && args.size() > 2)
				{
					
					boolean contains = Generic.containsTag(Generic.getAllUnitTags(u), args.get(1));					
					if(contains)
					{
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
						continue;
					}
				}
				else if(args.get(0).equals("racetag") && args.size() > 2)
				{

					boolean contains = Generic.containsTag(u.race.tags, args.get(1));					
					if(contains)
					{
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
						continue;
					}
				}
				else if(args.get(0).equals("unittheme") && args.size() > 2)
				{

					boolean not = args.contains("not");
					
					boolean contains = Generic.containsTag(u.race.themes, args.get(args.size() - 2));		
					
					for(Filter fs : u.race.themefilters)
						if(Generic.containsTag(fs.themes, args.get(args.size() - 2)))
							contains = true;
					
					for(Filter fs : u.appliedFilters)
						if(Generic.containsTag(fs.themes, args.get(args.size() - 2)))
							contains = true;
					
					if(contains != not)
					{
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
						continue;
					}
				}
				else if(args.get(0).equals("racetheme") && args.size() > 2)
				{

					boolean contains = Generic.containsTag(u.race.themes, args.get(1));					
					if(contains)
					{
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
						continue;
					}
				}
				else if(args.get(0).equals("posetag") && args.size() > 2)
				{

					boolean contains = Generic.containsTag(u.pose.tags, args.get(1));					
					if(contains)
					{
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
						continue;
					}
				}
				else if(args.get(0).equals("posetheme") && args.size() > 2)
				{

					boolean contains = Generic.containsTag(u.pose.themes, args.get(1));					
					if(contains)
					{
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
						continue;
					}
				}
				else if(args.get(0).equals("tag") && args.size() > 2)
				{

					boolean contains = Generic.containsTag(u.race.tags, args.get(1)) || Generic.containsTag(u.tags, args.get(1)) || Generic.containsTag(u.pose.tags, args.get(1));					
					if(contains)
					{
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
						continue;
					}
				}
				else if(args.get(0).equals("slot") && args.size() > 3)
				{
					boolean not = args.contains("not");
					boolean armor = args.contains("armor");
					boolean weapon = args.contains("weapon");
					boolean contains = false;
					Item i = u.getSlot(args.get(args.size() - 3));
					if(i != null)
					{
					
						if(i.id.equals(args.get(args.size() - 2)))
						{
							contains = true;
						}
						else if(i.getClass() == CustomItem.class)
						{
				
							CustomItem ci = (CustomItem)i;
							if(ci.olditem != null && ci.olditem.id != null && ci.olditem.id.equals(args.get(args.size() - 2)))
							{
								contains = true;
							}
							else if((Integer.parseInt(i.id) >= 700 && !i.armor) || ((Integer.parseInt(i.id) >= 250 && i.armor)))
							{
								if(Generic.containsTag(i.tags, "OLDID") && Generic.getTagValue(i.tags, "OLDID").equals(args.get(args.size() - 2)))
								{
									contains = true;
								}
							}
						}
				
						
						if(contains)
						{
							if(armor && !i.armor)
								contains = false;
							if(weapon && i.armor)
								contains = false;
						}
					}

					
					if(contains != not)
					{
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
						continue;
					}
				}
				else if(args.get(0).equals("slotname") && args.size() > 3)
				{
					boolean not = args.contains("not");
					boolean armor = args.contains("armor");
					boolean weapon = args.contains("weapon");
					boolean contains = false;
					Item i = u.getSlot(args.get(args.size() - 3));
					if(i != null)
					{
						if(i.name.equals(args.get(args.size() - 2)))
						{
							contains = true;
						}
		
						
						if(contains)
						{
							if(armor && !i.armor)
								contains = false;
							if(weapon && i.armor)
								contains = false;
						}
					}

					
					if(contains != not)
					{
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
						continue;
					}
				}
				else if(args.get(0).equals("slottag") && args.size() > 3)
				{

					boolean not = args.contains("not");
					boolean contains = false;
					Item i = u.getSlot(args.get(args.size() - 3));
					if(i != null)
					{
						if(i.tags.contains(args.get(args.size() - 2)))
						{
							contains = true;
						}
					}

					
					if(contains != not)
					{
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
						continue;
					}
				}
				else if(args.get(0).equals("slottagvalue") && args.size() > 4)
				{

					boolean not = args.contains("not");
					boolean contains = false;
					Item i = u.getSlot(args.get(args.size() - 4));
					if(i != null)
					{

						String value = Generic.getTagValue(i.tags, args.get(args.size() - 3));

						if(value != null && args.get(args.size() - 2).equals(value))
						{
							contains = true;
						}
					}
					
					
					if(contains != not)
					{
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
						continue;
					}
				}
				else if(args.get(0).equals("itemtag") && args.size() > 2)
				{

					boolean not = args.contains("not");
					boolean contains = false;
					for(Item i : u.slotmap.values())
						if(i != null)
							if(Generic.containsTag(i.tags, args.get(args.size() - 2)))
							{
								contains = true;
								break;
							}
					
					if(contains != not)
					{
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
						continue;
					}
				}
				else if(args.get(0).equals("itemtheme") && args.size() > 2)
				{

					boolean not = args.contains("not");
					boolean contains = false;
					for(Item i : u.slotmap.values())
					{
						// Items can be null if they're removed, offhand for example.
						if(i != null && Generic.containsTag(i.themes, args.get(args.size() - 2)))
						{
							contains = true;
							break;
						}
					}
					
					if(contains != not)
					{
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
						continue;
					}
				}
				else if(args.get(0).equals("poseitemtheme") && args.size() > 3)
				{

					boolean not = args.contains("not");
					boolean contains = false;
					
					ItemSet stuff = u.pose.getItems(args.get(args.size() - 3));
					if(stuff == null)
						contains = false;
					else
					{
						for(Item i : stuff)
							if(Generic.containsTag(i.themes, args.get(args.size() - 2)))
							{
								contains = true;
								break;
							}
					}
					
					if(contains != not)
					{
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
						continue;
					}
				}
				else if(args.get(0).equals("hasitem") && args.size() > 3)
				{

					boolean not = args.contains("not");
					boolean contains = u.getSlot(args.get(args.size() - 3)).equals(args.get(args.size() - 2));
					
					if(contains != not)
					{
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
						continue;
					}
				}
				else if(args.get(0).equals("taggedmagic") && args.size() > 1)
				{
			
					List<String> tags = new ArrayList<String>();
					
					tags.addAll(u.tags);
					for(Filter fu : u.appliedFilters)
						tags.addAll(fu.tags);
					for(String slot : u.slotmap.keySet())
					{
						if(u.slotmap.get(slot) != null)
							tags.addAll(u.slotmap.get(slot).tags);
					}
					
					boolean contains = true;
					for(int i = 1; i < args.size() - 1; i++)
					{
						String path = args.get(i);
						if(!tags.contains("path " + path))
							contains = false;
					}
					
					
					if(contains)
					{
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
						continue;
					}
					
				}
				// "comparemagic earth above air 1"
				// "comparemagic earth equal air 1"
				// "comparemagic earth below air 1"
				else if(args.get(0).equals("comparemagic") && args.size() > 4)
				{
					int path1 = Generic.PathToInteger(args.get(1));
					int path2 = Generic.PathToInteger(args.get(3));
					
		
					int[] picks = u.getMagicPicks();
					
					boolean check = false;
					
					if(args.get(2).equals("below")  && args.size() <= 5)
						check = (picks[path1] < picks[path2]);
					else if(args.get(2).equals("above") && args.size() <= 5)
						check = (picks[path1] > picks[path2]);
					else if(args.get(2).equals("equal"))
						check = (picks[path1] == picks[path2]);
					else if(args.size() > 5 && args.get(2).equals("below"))
					{
						int amount = Integer.parseInt(args.get(4));
						check = (picks[path2] - picks[path1] <= amount) && picks[path2] > picks[path1];
					}
					else if(args.size() > 5 && args.get(2).equals("above"))
					{
						int amount = Integer.parseInt(args.get(4));
						check = (picks[path1] - picks[path2] <= amount) && picks[path2] < picks[path1];
					}	
					
					else
						System.out.println("Error with " + str + ". No keyword below/above/equal was found.");

					
					if(check)
					{
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
						continue;
					}
						
					
				}
				else if(args.get(0).equals("personalmagic") && args.size() > 1)
				{
					
	
					
					boolean contains = true;
					int level = 1;
					
					int step = 0;
					int path = -1;
					boolean not = false;

					for(int i = 1; i < args.size() - 1; i++)
					{
						
						if(step == 0)
						{
							path = Generic.PathToInteger(args.get(i));
							

							
							// If there's no level specified
							if((i < args.size() - 2 && !Generic.isNumeric(args.get(i + 1)) && !args.get(i+1).equals("below")) || i == args.size() - 2)
							{
								if(u.getMagicPicks()[path] < 1)
								{
									contains = false;
									break;
								}	
							}
							else
							{
						
								step++;
							}
							
							
						}
						else if(step == 1)
						{

							if(args.get(i).equals("below"))
							{
								if(i != args.size() - 2)
								{
									level = Integer.parseInt(args.get(i+1));
								}
								else
								{
									System.out.println("Error in chanceinc " + str + ". Need a path level after keyword 'below'");
									level = 0;
								}
								not = true;
								continue;
							}
							else
								level = Integer.parseInt(args.get(i));		

		
							if((u.getMagicPicks()[path] < level) != not)
							{
								contains = false;
								not = false;
								break;
							}
							step--;
						}
			
		
					}
					
					if(contains)
					{

						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
						continue;
					}
				}
				else if(args.get(0).equals("magicbelow") && args.size() > 1)
				{

					boolean contains = true;
					int level = 1;
					
					int step = 0;
					int path = -1;
					for(int i = 1; i < args.size() - 1; i++)
					{
						if(step == 0)
						{
							path = Generic.PathToInteger(args.get(i));
							step++;
						}
						else if(step == 1)
						{
							level = Integer.parseInt(args.get(i));		
							if(u.getMagicPicks()[path] > level)
							{
								contains = false;
								break;
							}
							step--;
						}
			
		
					}
					
					if(contains)
					{
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
						continue;
					}
				}
				
				else if(args.get(0).equals("origname") && args.size() > 1)
				{

					boolean contains = false;
					if(u.name.type.equals(args.get(1)))
						contains = true;
					
					if(contains)
					{
						
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
						continue;
					}
				}
				else if(args.get(0).equals("race") && args.size() >= 3)
				{
					boolean canIncrease = false;
					boolean not = args.contains("not");
					if(u.race.name.toLowerCase().equals(args.get(args.size() - 2).toLowerCase()))
					{
						canIncrease = true;
					}
					if(canIncrease != not)
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
				}
				else if(args.get(0).equals("prot") && args.size() >= 3)
				{
					boolean canIncrease = false;
					
					boolean below = false;
					if(args.contains("below"))
						below = true;
					
					if(!below && u.getTotalProt(true) >= Integer.parseInt(args.get(args.size() - 2)))
					{
						canIncrease = true;
					}
					else if(below && u.getTotalProt(true) < Integer.parseInt(args.get(args.size() - 2)))
					{
						canIncrease = true;
					}
					if(canIncrease)
					{
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
					}
				}
				else if(args.get(0).equals("enc") && args.size() >= 2)
				{
					boolean canIncrease = false;
					
					boolean below = false;
					if(args.contains("below"))
						below = true;
					
					int enc = u.getTotalEnc();
					
					
					if(!below && enc >= Integer.parseInt(args.get(args.size() - 2)))
					{
						canIncrease = true;
					}
					else if(below && enc < Integer.parseInt(args.get(args.size() - 2)))
					{
						canIncrease = true;
					}
					if(canIncrease)
						applyChanceInc(filters, f,  (args.get(args.size() - 1)));
				}
				
				
			}
			

		}
		

		
	}
	
}
