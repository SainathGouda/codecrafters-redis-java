package command;

import constant.RESPConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommandParser {
    public static class RedisCommandParser{
        private final BufferedReader inputStream;

        public RedisCommandParser(BufferedReader inputStream){
            this.inputStream = inputStream;
        }

        public CommandWithArgs parseCommand(String firstLine) throws IOException{
            if(firstLine == null || !firstLine.startsWith(RESPConstants.ARRAY_PREFIX)){
                throw new IOException("Invalid command format");
            }

            //Parse number of arguments (*3)
            int numOfArgs = Integer.parseInt(firstLine.substring(1));
            List<String> arguments = new ArrayList<>();

            //Loading each argument
            for(int i=0;i<numOfArgs;i++){
                inputStream.readLine(); // ($3) length of argument
                String argument = inputStream.readLine();
                arguments.add(argument);
            }

            if (arguments.isEmpty()) {
                throw new IOException("Command not found in input.");
            }

            return new CommandWithArgs(arguments);
        }
    }

    public static class CommandWithArgs{
        private final List<String> arguments;

        public CommandWithArgs(List<String> arguments){
            this.arguments = arguments;
        }

        public String getCommand(){
            return arguments.getFirst().toUpperCase();
        }

        public List<String> getArguments(){
            return arguments.subList(1, arguments.size());
        }

        public String getKey(){
            return arguments.size() > 1 ? arguments.get(1) : null;
        }

        public String getValue(){
            return arguments.size() > 2 ? arguments.get(2) : null;
        }

        public long getTTL(){
            if(arguments.size()>4 && "PX".equalsIgnoreCase(arguments.get(3))){
                return Long.parseLong(arguments.get(4));
            }
            return -1;
        }
    }
}
