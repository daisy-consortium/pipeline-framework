#!/usr/bin/env ruby
require 'rubygems'
require 'optparse'
require 'nokogiri'
require './core/dp2'
require './core/conf'
require './core/scripts'
require './dynamic_commands'
require './help_command.rb'

def main

	command=checkargs
	Ctxt.conf("config.yml")
	cmds={}
	#dynamic commads
	DynamicCommands.get.each{|cmd| cmds[cmd.name]=cmd}
	#static commads
	cmds["help"]=HelpCommand.new(cmds)
	error=""
	hasErr=false

	if command!=nil && cmds.has_key?(command)
		begin
			cmds[command].execute(ARGV[1..-1])
		rescue Exception=>e
			error=e.message
			hasErr=true		
		end
	else
		hasErr=true
	end

	if hasErr
		puts "\nERROR: #{error}\n\n"
		puts cmds["help"].help
	end
end

def checkargs
  cmd=nil
  ARGV.each do |a|
    cmd = a
    break
  end
  return cmd
end

# execution starts here
main 

